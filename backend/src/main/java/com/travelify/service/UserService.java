package com.travelify.service;

import com.travelify.dto.UserDtos;
import com.travelify.exception.ResourceNotFoundException;
import com.travelify.model.User;
import com.travelify.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserDtos> listAll() {
        return userRepository.findAll().stream()
                .map(u -> UserDtos.builder()
                        .id(u.getId())
                        .email(u.getEmail())
                        .firstName(u.getFirstName())
                        .lastName(u.getLastName())
                        .fullName(u.getFullName())
                        .phone(u.getPhone())
                        .avatarUrl(u.getAvatarUrl())
                        .role(u.getRole())
                        .isActive(u.getIsActive())
                        .createdAt(u.getCreatedAt())
                        .lastLoginAt(u.getLastLoginAt())
                        .build())
                .toList();
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User with email " + email + " not found."));
    }
}