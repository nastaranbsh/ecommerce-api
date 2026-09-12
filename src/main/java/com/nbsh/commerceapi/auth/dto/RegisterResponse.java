package com.nbsh.commerceapi.auth.dto;

import java.time.Instant;

public record RegisterResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        Instant createdAt
) {
}