package com.nbsh.commerceapi.common.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
        @Schema(
                description = "Time the error was generated",
                example = "2026-09-28T14:20:15.123Z"
        )
        Instant timestamp,

        @Schema(
                example = "400"
        )
        int status,

        @Schema(
                example = "Bad Request"
        )
        String error,

        @Schema(
                example = "Validation failed"
        )
        String message,

        @Schema(
                example = "/api/v1/products"
        )
        String path,

        List<ValidationError> validationErrors
) {
}