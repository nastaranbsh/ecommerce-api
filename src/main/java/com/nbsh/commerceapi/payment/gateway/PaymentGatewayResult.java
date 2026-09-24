package com.nbsh.commerceapi.payment.gateway;

public record PaymentGatewayResult(
        boolean successful,
        String gatewayReference,
        String failureCode,
        String failureMessage
) {

    public static PaymentGatewayResult success(
            String reference
    ) {
        return new PaymentGatewayResult(
                true,
                reference,
                null,
                null
        );
    }

    public static PaymentGatewayResult declined(
            String code,
            String message
    ) {
        return new PaymentGatewayResult(
                false,
                null,
                code,
                message
        );
    }
}