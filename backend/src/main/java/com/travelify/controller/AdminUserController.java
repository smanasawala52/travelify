package com.travelify.controller;

import com.travelify.dto.AdminUserDtos;
import com.travelify.dto.UserDtos;
import com.travelify.model.Role;
import com.travelify.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Admin user management — aligns with WordPress / WP Travel role administration.
 */
@RestController
@RequestMapping("/api/admin/users")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin Users", description = "Admin-only user directory and role/status management")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    @Operation(summary = "List users", description = "Paginated; filter by role, active status, and search text.")
    public Page<UserDtos> listUsers(
            @Parameter(description = "Filter by role") @RequestParam(required = false) Role role,
            @Parameter(description = "Filter by active flag") @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "Search email / first / last name") @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return adminUserService.listUsers(role, isActive, search, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by id")
    public UserDtos getUser(@PathVariable Long id) {
        return adminUserService.getUser(id);
    }

    @PutMapping("/{id}/role")
    @Operation(summary = "Change user role")
    public UserDtos updateRole(@PathVariable Long id,
                               @Valid @RequestBody AdminUserDtos.UpdateRoleRequest request) {
        return adminUserService.updateRole(id, request);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Enable or disable user")
    public UserDtos updateStatus(@PathVariable Long id,
                                 @Valid @RequestBody AdminUserDtos.UpdateStatusRequest request) {
        return adminUserService.updateStatus(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete user")
    public void deleteUser(@PathVariable Long id) {
        adminUserService.deleteUser(id);
    }
}
