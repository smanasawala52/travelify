package com.travelify.controller;

import com.travelify.dto.AuthDtos;
import com.travelify.dto.UserDtos;
import com.travelify.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Auth API backing WP Travel–style account flows: register/login, profile,
 * password management, and logout.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User registration, login, and account management")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new user", description = "Public. Defaults to CUSTOMER role.")
    public UserDtos register(@Valid @RequestBody AuthDtos.RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Public. Returns access + refresh JWTs and user profile.")
    public AuthDtos.AuthResponse login(@Valid @RequestBody AuthDtos.LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Refresh access token",
            description = "Authenticated. Provide refresh token in body; requires a valid access token.")
    public AuthDtos.TokenResponse refresh(@Valid @RequestBody AuthDtos.RefreshTokenRequest request) {
        return authService.refreshToken(request.getRefreshToken());
    }

    @PostMapping("/logout")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Logout", description = "Blacklists the current access token (client should discard tokens).")
    public AuthDtos.MessageResponse logout(HttpServletRequest request) {
        authService.logout(extractBearer(request));
        return AuthDtos.MessageResponse.builder().message("Logged out").build();
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Current user profile")
    public UserDtos me() {
        return authService.getCurrentUser();
    }

    @PutMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update current user profile")
    public UserDtos updateMe(@Valid @RequestBody AuthDtos.UpdateProfileRequest request) {
        return authService.updateMyProfile(request);
    }

    @PutMapping("/change-password")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Change current user password")
    public AuthDtos.MessageResponse changePassword(@Valid @RequestBody AuthDtos.ChangePasswordRequest request) {
        return authService.changeMyPassword(request);
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset email", description = "Public. Always returns a generic success message.")
    public AuthDtos.MessageResponse forgotPassword(@Valid @RequestBody AuthDtos.ForgotPasswordRequest request) {
        return authService.forgotPassword(request.getEmail());
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password with token", description = "Public.")
    public AuthDtos.MessageResponse resetPassword(@Valid @RequestBody AuthDtos.ResetPasswordRequest request) {
        return authService.resetPassword(request.getToken(), request.getNewPassword());
    }

    private String extractBearer(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
