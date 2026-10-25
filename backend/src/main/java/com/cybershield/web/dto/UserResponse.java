package com.cybershield.web.dto;

import com.cybershield.model.User;

import java.time.LocalDateTime;

public record UserResponse(
        String userId,
        String username,
        String email,
        String fullName,
        LocalDateTime registeredAt,
        LocalDateTime lastLoginAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getRegisteredAt(),
                user.getLastLoginAt()
        );
    }
}
