package com.nbsh.commerceapi.payment.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class FakePaymentGateway
        implements PaymentGateway {

    private final String outcome;

    private final Map<String, PaymentGatewayResult>
            completedResults =
            new ConcurrentHashMap<>();

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

        PaymentGatewayResult existing =
                completedResults.get(
                        command.idempotencyKey()
                );

        if (existing != null) {
            return existing;
        }

        return switch (outcome) {

            case "SUCCESS" ->
                    processSuccess(command);

            case "DECLINE" ->
                    processDecline(command);

            case "TIMEOUT" ->
                    throw new PaymentGatewayException(
                            "Payment gateway timed out before a result was known"
                    );

            case "TIMEOUT_AFTER_SUCCESS" ->
                    processSuccessThenTimeout(
                            command
                    );

            default ->
                    throw new IllegalStateException(
                            "Unsupported fake payment outcome: "
                                    + outcome
                    );
        };
    }

    private PaymentGatewayResult processSuccess(
            PaymentGatewayCommand command
    ) {

        return completedResults
                .computeIfAbsent(
                        command.idempotencyKey(),
                        key ->
                                PaymentGatewayResult
                                        .success(
                                                deterministicReference(
                                                        key
                                                )
                                        )
                );
    }

    private PaymentGatewayResult processDecline(
            PaymentGatewayCommand command
    ) {

        return completedResults
                .computeIfAbsent(
                        command.idempotencyKey(),
                        key ->
                                PaymentGatewayResult
                                        .declined(
                                                "CARD_DECLINED",
                                                "The payment was declined"
                                        )
                );
    }

    private PaymentGatewayResult
    processSuccessThenTimeout(
            PaymentGatewayCommand command
    ) {

        boolean alreadyProcessed =
                completedResults.containsKey(
                        command.idempotencyKey()
                );

        PaymentGatewayResult result =
                processSuccess(command);

        if (alreadyProcessed) {
            return result;
        }

        throw new PaymentGatewayException(
                "Gateway processed the payment, but the response was lost"
        );
    }

    private String deterministicReference(
            String idempotencyKey
    ) {

        UUID uuid =
                UUID.nameUUIDFromBytes(
                        idempotencyKey
                                .getBytes(
                                        StandardCharsets.UTF_8
                                )
                );

        return "FAKE-" + uuid;
    }
}