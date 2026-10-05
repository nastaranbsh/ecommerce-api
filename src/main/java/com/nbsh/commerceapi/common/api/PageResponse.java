package com.nbsh.commerceapi.common.api;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record PageResponse<T>(
        @Schema(
                description = "Current page content"
        )
        List<T> content,

        @Schema(
                description = "Zero-based page number",
                example = "0"
        )
        int page,

        @Schema(
                example = "20"
        )
        int size,

        @Schema(
                example = "125"
        )
        long totalElements,

        @Schema(
                example = "7"
        )
        int totalPages,

        boolean first,

        boolean last
) {
}