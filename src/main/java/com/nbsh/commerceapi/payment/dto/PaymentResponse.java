package com.nbsh.commerceapi.payment.dto;

import com.nbsh.commerceapi.payment.PaymentStatus;

public record PaymentResponse(
        Long paymentAttemptId,
        Long orderId,
        String orderNumber,
        PaymentStatus status,
        String gatewayReference,
        String message,
        boolean replayed
) {
}