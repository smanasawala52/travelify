package com.travelify.service;

import com.travelify.dto.AuthDtos;
import com.travelify.dto.UserDtos;
import com.travelify.exception.AccountDisabledException;
import com.travelify.exception.ForbiddenOperationException;
import com.travelify.exception.InvalidCredentialsException;
import com.travelify.exception.InvalidTokenException;
import com.travelify.exception.TokenExpiredException;
import com.travelify.exception.UserAlreadyExistsException;
import com.travelify.exception.UserNotFoundException;
import com.travelify.model.PasswordResetToken;
import com.travelify.model.Role;
import com.travelify.model.User;
import com.travelify.repository.PasswordResetTokenRepository;
import com.travelify.repository.UserRepository;
import com.travelify.security.JwtUtil;
import com.travelify.security.TokenBlacklistService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class AuthService {

    private static final int RESET_TOKEN_HOURS = 1;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final TokenBlacklistService tokenBlacklistService;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository,
                       PasswordResetTokenRepository passwordResetTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       AuthenticationManager authenticationManager,
                       TokenBlacklistService tokenBlacklistService,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.tokenBlacklistService = tokenBlacklistService;
        this.emailService = emailService;
    }

    @Transactional
    public UserDtos register(AuthDtos.RegisterRequest request) {
        String email = normalizeEmail(request.getEmail());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new UserAlreadyExistsException("Email already registered");
        }

        Role role = request.getRole() == null ? Role.CUSTOMER : request.getRole();
        if (role == Role.ADMIN) {
            throw new ForbiddenOperationException("Cannot self-register as ADMIN");
        }

        User user = userRepository.save(User.builder()
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .role(role)
                .isActive(true)
                .build());

        return toUserDto(user);
    }

    @Transactional
    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        String email = normalizeEmail(request.getEmail());
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getPassword()));
        } catch (BadCredentialsException ex) {
            throw new InvalidCredentialsException("Invalid email or password");
        } catch (DisabledException ex) {
            throw new AccountDisabledException("Account is deactivated");
        }

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        ensureActive(user);
        user.setLastLoginAt(Instant.now());
        return toAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthDtos.TokenResponse refreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new InvalidTokenException("Refresh token is required");
        }

        String jti = jwtUtil.extractJti(refreshToken);
        if (tokenBlacklistService.isBlacklisted(jti)) {
            throw new InvalidTokenException("Refresh token has been revoked");
        }
        if (!jwtUtil.isRefreshToken(refreshToken)) {
            throw new InvalidTokenException("Invalid refresh token");
        }
        if (jwtUtil.isTokenExpired(refreshToken)) {
            throw new TokenExpiredException("Refresh token has expired");
        }

        String email = jwtUtil.extractEmail(refreshToken);
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        ensureActive(user);

        String accessToken = jwtUtil.generateAccessToken(user);
        return AuthDtos.TokenResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getAccessExpirationMs() / 1000)
                .build();
    }

    public void logout(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return;
        }
        try {
            String jti = jwtUtil.extractJti(accessToken);
            Instant expiresAt = jwtUtil.extractExpiration(accessToken).toInstant();
            tokenBlacklistService.blacklist(jti, expiresAt);
        } catch (TokenExpiredException | InvalidTokenException ignored) {
            // Already unusable — nothing to blacklist
        }
        SecurityContextHolder.clearContext();
    }

    @Transactional(readOnly = true)
    public UserDtos getCurrentUser() {
        return toUserDto(requireCurrentUser());
    }

    @Transactional
    public UserDtos updateMyProfile(AuthDtos.UpdateProfileRequest request) {
        User user = requireCurrentUser();
        return applyProfileUpdate(user, request);
    }

    @Transactional
    public UserDtos updateProfile(Long userId, AuthDtos.UpdateProfileRequest request) {
        User current = requireCurrentUser();
        if (!current.getId().equals(userId) && current.getRole() != Role.ADMIN) {
            throw new ForbiddenOperationException("Cannot update another user's profile");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return applyProfileUpdate(user, request);
    }

    @Transactional
    public AuthDtos.MessageResponse changeMyPassword(AuthDtos.ChangePasswordRequest request) {
        User user = requireCurrentUser();
        return applyPasswordChange(user, request);
    }

    @Transactional
    public AuthDtos.MessageResponse changePassword(Long userId, AuthDtos.ChangePasswordRequest request) {
        User current = requireCurrentUser();
        if (!current.getId().equals(userId) && current.getRole() != Role.ADMIN) {
            throw new ForbiddenOperationException("Cannot change another user's password");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return applyPasswordChange(user, request);
    }

    private UserDtos applyProfileUpdate(User user, AuthDtos.UpdateProfileRequest request) {
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        return toUserDto(user);
    }

    private AuthDtos.MessageResponse applyPasswordChange(User user, AuthDtos.ChangePasswordRequest request) {
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        return AuthDtos.MessageResponse.builder().message("Password updated successfully").build();
    }

    @Transactional
    public AuthDtos.MessageResponse forgotPassword(String email) {
        String normalized = normalizeEmail(email);
        // Always return the same message to avoid email enumeration
        userRepository.findByEmailIgnoreCase(normalized).ifPresent(user -> {
            passwordResetTokenRepository.deleteByEmail(normalized);
            String token = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
            passwordResetTokenRepository.save(PasswordResetToken.builder()
                    .email(normalized)
                    .token(token)
                    .expiresAt(Instant.now().plus(RESET_TOKEN_HOURS, ChronoUnit.HOURS))
                    .build());
            emailService.sendPasswordResetEmail(normalized, token);
        });
        return AuthDtos.MessageResponse.builder()
                .message("If an account exists for that email, a reset link has been sent")
                .build();
    }

    @Transactional
    public AuthDtos.MessageResponse resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid or unknown reset token"));

        if (resetToken.isExpired()) {
            passwordResetTokenRepository.delete(resetToken);
            throw new TokenExpiredException("Password reset token has expired");
        }

        User user = userRepository.findByEmailIgnoreCase(resetToken.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        passwordResetTokenRepository.deleteByEmail(resetToken.getEmail());
        return AuthDtos.MessageResponse.builder().message("Password has been reset").build();
    }

    public User requireCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new InvalidCredentialsException("Not authenticated");
        }
        String email = authentication.getName();
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        ensureActive(user);
        return user;
    }

    public UserDtos toUserDto(User user) {
        return UserDtos.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }

    private AuthDtos.AuthResponse toAuthResponse(User user) {
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);
        return AuthDtos.AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .token(accessToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getAccessExpirationMs() / 1000)
                .user(toUserDto(user))
                .build();
    }

    private void ensureActive(User user) {
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new AccountDisabledException("Account is deactivated");
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
