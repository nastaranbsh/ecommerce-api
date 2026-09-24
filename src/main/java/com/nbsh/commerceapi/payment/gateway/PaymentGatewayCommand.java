package com.nbsh.commerceapi.payment.gateway;

import java.math.BigDecimal;

public record PaymentGatewayCommand(
        String orderNumber,
        BigDecimal amount
) {
}