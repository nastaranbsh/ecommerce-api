package com.nbsh.commerceapi.product.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        String sku,
        boolean active,
        CategorySummaryResponse category,
        Instant createdAt,
        Instant updatedAt
) {
}