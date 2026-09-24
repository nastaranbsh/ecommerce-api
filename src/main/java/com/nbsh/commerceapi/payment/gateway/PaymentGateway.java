package com.nbsh.commerceapi.payment.gateway;

public interface PaymentGateway {

    PaymentGatewayResult charge(
            PaymentGatewayCommand command
    );
}