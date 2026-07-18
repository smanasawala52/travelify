package com.travelify.dto;

import com.travelify.model.Role;
import com.travelify.validation.StrongPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

public final class AuthDtos {
    private AuthDtos() {}

    @Data
    @Schema(name = "RegisterRequest")
    public static class RegisterRequest {
        @NotBlank
        @Email(message = "Email must be a valid address")
        @Schema(example = "new.customer@travelify.com")
        private String email;

        @NotBlank
        @StrongPassword
        @Schema(example = "password123", description = "Min 8 chars, letter + digit")
        private String password;

        @NotBlank
        @Size(max = 100)
        @Schema(example = "Alex")
        private String firstName;

        @NotBlank
        @Size(max = 100)
        @Schema(example = "Rivera")
        private String lastName;

        @Size(max = 30)
        @Pattern(regexp = "^$|^[+0-9()\\-\\s]{7,30}$", message = "Phone number format is invalid")
        @Schema(example = "+1-555-0142")
        private String phone;

        @Schema(description = "Optional; defaults to CUSTOMER. ADMIN self-registration is rejected.",
                allowableValues = {"CUSTOMER", "AGENT"})
        private Role role;
    }

    @Data
    @Schema(name = "LoginRequest")
    public static class LoginRequest {
        @NotBlank
        @Email
        @Schema(example = "customer@travelify.com")
        private String email;

        @NotBlank
        @Schema(example = "password123")
        private String password;
    }

    @Data
    @Schema(name = "RefreshTokenRequest")
    public static class RefreshTokenRequest {
        @NotBlank
        @Schema(description = "Refresh JWT issued at login")
        private String refreshToken;
    }

    @Data
    @Schema(name = "UpdateProfileRequest")
    public static class UpdateProfileRequest {
        @Size(max = 100)
        private String firstName;

        @Size(max = 100)
        private String lastName;

        @Size(max = 30)
        @Pattern(regexp = "^$|^[+0-9()\\-\\s]{7,30}$", message = "Phone number format is invalid")
        private String phone;

        @Size(max = 512)
        private String avatarUrl;
    }

    @Data
    @Schema(name = "ChangePasswordRequest")
    public static class ChangePasswordRequest {
        @NotBlank
        private String oldPassword;

        @NotBlank
        @StrongPassword
        private String newPassword;
    }

    @Data
    @Schema(name = "ForgotPasswordRequest")
    public static class ForgotPasswordRequest {
        @NotBlank
        @Email
        private String email;
    }

    @Data
    @Schema(name = "ResetPasswordRequest")
    public static class ResetPasswordRequest {
        @NotBlank
        private String token;

        @NotBlank
        @StrongPassword
        private String newPassword;
    }

    @Data
    @Builder
    @Schema(name = "AuthResponse")
    public static class AuthResponse {
        private String accessToken;
        private String refreshToken;
        @Schema(description = "Alias for accessToken (legacy clients)")
        private String token;
        private String tokenType;
        private long expiresIn;
        private UserDtos user;
    }

    @Data
    @Builder
    @Schema(name = "TokenResponse")
    public static class TokenResponse {
        private String accessToken;
        private String tokenType;
        private long expiresIn;
    }

    @Data
    @Builder
    @Schema(name = "MessageResponse")
    public static class MessageResponse {
        private String message;
    }
}
