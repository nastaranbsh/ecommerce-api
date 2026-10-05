package com.nbsh.commerceapi.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record CartItemResponse(
        Long id,
        Long productId,
        String productName,
        String sku,
        String categoryName,
        @Schema(
                description = """
                Current Product price. Cart prices are not historical
                snapshots and may change before checkout.
                """
        )
        BigDecimal unitPrice,
        int quantity,
        BigDecimal subtotal
) {
}