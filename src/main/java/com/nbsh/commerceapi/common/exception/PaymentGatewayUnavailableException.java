package com.nbsh.commerceapi.common.exception;

public class PaymentGatewayUnavailableException
        extends RuntimeException {

    public PaymentGatewayUnavailableException(
            String message
    ) {
        super(message);
    }
}