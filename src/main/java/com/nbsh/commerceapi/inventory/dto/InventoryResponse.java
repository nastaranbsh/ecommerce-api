package com.nbsh.commerceapi.inventory.dto;

import java.time.Instant;

public record InventoryResponse(
        Long productId,
        String productName,
        int quantityAvailable,
        Long version,
        Instant updatedAt
) {
}