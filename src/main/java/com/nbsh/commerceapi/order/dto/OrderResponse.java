package com.nbsh.commerceapi.order.dto;

import com.nbsh.commerceapi.order.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        String orderNumber,
        OrderStatus status,
        BigDecimal subtotal,
        BigDecimal totalAmount,
        List<OrderItemResponse> items,
        Instant createdAt,
        Instant updatedAt
) {
}