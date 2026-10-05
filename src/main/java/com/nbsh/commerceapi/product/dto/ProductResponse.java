package com.nbsh.commerceapi.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(
        Long id,
        String name,
        String description,
        @Schema(
                description = "Current catalog unit price",
                example = "129.99"
        )
        BigDecimal price,
        String sku,
        boolean active,
        CategorySummaryResponse category,
        Instant createdAt,
        Instant updatedAt
) {
}