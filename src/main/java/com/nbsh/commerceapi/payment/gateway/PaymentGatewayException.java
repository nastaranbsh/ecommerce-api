package com.nbsh.commerceapi.payment.gateway;

public class PaymentGatewayException
        extends RuntimeException {

    public PaymentGatewayException(
            String message
    ) {
        super(message);
    }
}