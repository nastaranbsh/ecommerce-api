package com.nbsh.commerceapi.common.exception;

import io.swagger.v3.oas.annotations.media.Schema;

public record ValidationError(
        @Schema(
                example = "price"
        )
        String field,

        @Schema(
                example = "Price must be greater than or equal to 0"
        )
        String message
) {
}