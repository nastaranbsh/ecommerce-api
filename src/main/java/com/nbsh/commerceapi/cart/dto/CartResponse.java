package com.nbsh.commerceapi.cart.dto;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
        Long id,
        List<CartItemResponse> items,
        int totalQuantity,
        BigDecimal totalPrice
) {
}