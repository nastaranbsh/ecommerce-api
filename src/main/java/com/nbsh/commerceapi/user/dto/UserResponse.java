package com.nbsh.commerceapi.user.dto;

import com.nbsh.commerceapi.user.UserRole;

import java.time.Instant;

public record UserResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        UserRole role,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}