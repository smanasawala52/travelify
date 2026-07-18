package com.travelify.service;

import com.travelify.dto.AdminUserDtos;
import com.travelify.dto.UserDtos;
import com.travelify.exception.ForbiddenOperationException;
import com.travelify.exception.UserNotFoundException;
import com.travelify.model.Role;
import com.travelify.model.User;
import com.travelify.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final AuthService authService;

    public AdminUserService(UserRepository userRepository, AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

    @Transactional(readOnly = true)
    public Page<UserDtos> listUsers(Role role, Boolean isActive, String search, Pageable pageable) {
        String q = (search == null || search.isBlank()) ? null : search.trim();
        return userRepository.searchUsers(role, isActive, q, pageable)
                .map(authService::toUserDto);
    }

    @Transactional(readOnly = true)
    public UserDtos getUser(Long id) {
        return authService.toUserDto(find(id));
    }

    @Transactional
    public UserDtos updateRole(Long id, AdminUserDtos.UpdateRoleRequest request) {
        User actor = authService.requireCurrentUser();
        User target = find(id);

        if (actor.getId().equals(target.getId()) && request.getRole() != Role.ADMIN) {
            throw new ForbiddenOperationException("Admins cannot demote themselves");
        }
        if (target.getRole() == Role.ADMIN && request.getRole() != Role.ADMIN
                && userRepository.countByRoleAndIsActiveTrue(Role.ADMIN) <= 1) {
            throw new ForbiddenOperationException("Cannot demote the last active admin");
        }

        target.setRole(request.getRole());
        return authService.toUserDto(target);
    }

    @Transactional
    public UserDtos updateStatus(Long id, AdminUserDtos.UpdateStatusRequest request) {
        User actor = authService.requireCurrentUser();
        User target = find(id);

        if (actor.getId().equals(target.getId()) && Boolean.FALSE.equals(request.getIsActive())) {
            throw new ForbiddenOperationException("Admins cannot disable themselves");
        }
        if (target.getRole() == Role.ADMIN
                && Boolean.TRUE.equals(target.getIsActive())
                && Boolean.FALSE.equals(request.getIsActive())
                && userRepository.countByRoleAndIsActiveTrue(Role.ADMIN) <= 1) {
            throw new ForbiddenOperationException("Cannot disable the last active admin");
        }

        target.setIsActive(request.getIsActive());
        return authService.toUserDto(target);
    }

    @Transactional
    public void deleteUser(Long id) {
        User actor = authService.requireCurrentUser();
        User target = find(id);

        if (actor.getId().equals(target.getId())) {
            throw new ForbiddenOperationException("Admins cannot delete themselves");
        }
        if (target.getRole() == Role.ADMIN
                && Boolean.TRUE.equals(target.getIsActive())
                && userRepository.countByRoleAndIsActiveTrue(Role.ADMIN) <= 1) {
            throw new ForbiddenOperationException("Cannot delete the last active admin");
        }

        userRepository.delete(target);
    }

    private User find(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }
}
