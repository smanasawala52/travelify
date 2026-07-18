package com.travelify.dto;

import com.travelify.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@Schema(name = "UserResponse")
public class UserDtos {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phone;
    private String avatarUrl;
    private Role role;
    private Boolean isActive;
    private Instant createdAt;
    private Instant lastLoginAt;
}
