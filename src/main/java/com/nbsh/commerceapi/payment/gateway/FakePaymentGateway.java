package com.nbsh.commerceapi.payment.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.UUID;

@Component
public class FakePaymentGateway
        implements PaymentGateway {

    private final String outcome;

    public FakePaymentGateway(
            @Value("${payment.fake.outcome:SUCCESS}")
            String outcome
    ) {
        this.outcome =
                outcome.trim()
                        .toUpperCase(Locale.ROOT);
    }

    @Override
    public PaymentGatewayResult charge(
            PaymentGatewayCommand command
    ) {

        return switch (outcome) {

            case "SUCCESS" ->
                    PaymentGatewayResult.success(
                            "FAKE-"
                                    + UUID.randomUUID()
                    );

            case "DECLINE" ->
                    PaymentGatewayResult.declined(
                            "CARD_DECLINED",
                            "The payment was declined"
                    );

            case "TIMEOUT" ->
                    throw new PaymentGatewayException(
                            "Payment gateway timed out"
                    );

            default ->
                    throw new IllegalStateException(
                            "Unsupported fake payment outcome: "
                                    + outcome
                    );
        };
    }
}