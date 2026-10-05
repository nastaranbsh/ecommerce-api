package com.nbsh.commerceapi.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        Long productId,
        String productName,
        String productSku,
        @Schema(
                description = """
                Unit price snapshotted at checkout. Historical Orders are
                unaffected by later Product price changes.
                """
        )
        BigDecimal unitPrice,
        int quantity,
        BigDecimal lineTotal
) {
}