package com.nbsh.commerceapi.payment;

import com.nbsh.commerceapi.common.exception.InvalidRequestException;
import com.nbsh.commerceapi.common.exception.PaymentGatewayUnavailableException;
import com.nbsh.commerceapi.payment.dto.PaymentResponse;
import com.nbsh.commerceapi.payment.gateway.PaymentGateway;
import com.nbsh.commerceapi.payment.gateway.PaymentGatewayCommand;
import com.nbsh.commerceapi.payment.gateway.PaymentGatewayException;
import com.nbsh.commerceapi.payment.gateway.PaymentGatewayResult;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private final PaymentPersistenceService persistenceService;
    private final PaymentGateway paymentGateway;

    public PaymentService(
            PaymentPersistenceService persistenceService,
            PaymentGateway paymentGateway
    ) {
        this.persistenceService =
                persistenceService;

        this.paymentGateway =
                paymentGateway;
    }

    public PaymentResponse pay(
            Long userId,
            Long orderId,
            String idempotencyKey
    ) {

        String key =
                validateIdempotencyKey(
                        idempotencyKey
                );

        PaymentPersistenceService.PreparedPayment prepared =
                persistenceService
                        .preparePayment(
                                userId,
                                orderId,
                                key
                        );

        if (prepared.status()
                == PaymentStatus.SUCCEEDED
                || prepared.status()
                == PaymentStatus.DECLINED) {

            return replayCompleted(
                    prepared
            );
        }

        PaymentGatewayResult result;

        try {

            result =
                    paymentGateway.charge(
                            new PaymentGatewayCommand(
                                    key,
                                    prepared.orderNumber(),
                                    prepared.amount()
                            )
                    );

        } catch (PaymentGatewayException exception) {

            persistenceService.markUnknown(
                    prepared.paymentAttemptId(),
                    exception.getMessage()
            );

            throw new PaymentGatewayUnavailableException(
                    "Payment result is currently unknown. Retry this request using the same Idempotency-Key."
            );
        }

        if (result.successful()) {

            return persistenceService
                    .markSucceeded(
                            prepared.paymentAttemptId(),
                            result.gatewayReference()
                    );
        }

        return persistenceService
                .markDeclinedAndRestoreStock(
                        prepared.paymentAttemptId(),
                        result.failureCode(),
                        result.failureMessage()
                );
    }

    private String validateIdempotencyKey(
            String idempotencyKey
    ) {

        if (idempotencyKey == null
                || idempotencyKey.isBlank()) {

            throw new InvalidRequestException(
                    "Idempotency-Key header is required"
            );
        }

        String normalized =
                idempotencyKey.trim();

        if (normalized.length() < 8
                || normalized.length() > 100) {

            throw new InvalidRequestException(
                    "Idempotency-Key must be between 8 and 100 characters"
            );
        }

        return normalized;
    }

    private PaymentResponse replayCompleted(
            PaymentPersistenceService.PreparedPayment prepared
    ) {

        return switch (prepared.status()) {

            case SUCCEEDED ->
                    new PaymentResponse(
                            prepared.paymentAttemptId(),
                            prepared.orderId(),
                            prepared.orderNumber(),
                            PaymentStatus.SUCCEEDED,
                            prepared.gatewayReference(),
                            "Payment succeeded",
                            true
                    );

            case DECLINED ->
                    new PaymentResponse(
                            prepared.paymentAttemptId(),
                            prepared.orderId(),
                            prepared.orderNumber(),
                            PaymentStatus.DECLINED,
                            null,
                            prepared.failureMessage(),
                            true
                    );

            default ->
                    throw new IllegalStateException(
                            "Payment is not in a replayable final state"
                    );
        };
    }
}