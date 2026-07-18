package com.travelify.dto;

import com.travelify.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

public final class AdminUserDtos {
    private AdminUserDtos() {}

    @Data
    @Schema(name = "UpdateRoleRequest")
    public static class UpdateRoleRequest {
        @NotNull
        @Schema(allowableValues = {"CUSTOMER", "AGENT", "ADMIN"})
        private Role role;
    }

    @Data
    @Schema(name = "UpdateStatusRequest")
    public static class UpdateStatusRequest {
        @NotNull
        @Schema(description = "true = enable, false = disable")
        private Boolean isActive;
    }
}
