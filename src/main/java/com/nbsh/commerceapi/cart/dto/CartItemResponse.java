package com.nbsh.commerceapi.cart.dto;

import java.math.BigDecimal;

public record CartItemResponse(
        Long id,
        Long productId,
        String productName,
        String sku,
        String categoryName,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal subtotal
) {
}