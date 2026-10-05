package com.nbsh.commerceapi.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginResponse(
        @Schema(
                description = "JWT access token",
                example = "eyJhbGciOiJIUzI1NiJ9..."
        )
        String accessToken,

        @Schema(
                example = "Bearer"
        )
        String tokenType,

        @Schema(
                description = "Token lifetime in seconds",
                example = "900"
        )
        long expiresIn
) {
}