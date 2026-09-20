package com.nbsh.commerceapi.order.dto;

import com.nbsh.commerceapi.order.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderSummaryResponse(
        Long id,
        String orderNumber,
        OrderStatus status,
        BigDecimal totalAmount,
        Instant createdAt
) {
}