package com.nbsh.commerceapi.payment;

import com.nbsh.commerceapi.common.exception.InvalidRequestException;
import com.nbsh.commerceapi.common.exception.PaymentGatewayUnavailableException;
import com.nbsh.commerceapi.observability.CommerceMetrics;
import com.nbsh.commerceapi.payment.dto.PaymentResponse;
import com.nbsh.commerceapi.payment.gateway.PaymentGateway;
import com.nbsh.commerceapi.payment.gateway.PaymentGatewayCommand;
import com.nbsh.commerceapi.payment.gateway.PaymentGatewayException;
import com.nbsh.commerceapi.payment.gateway.PaymentGatewayResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class PaymentService {

    private final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private final CommerceMetrics commerceMetrics;

    private final PaymentPersistenceService persistenceService;
    private final PaymentGateway paymentGateway;


    public PaymentService(
            PaymentPersistenceService persistenceService,
            PaymentGateway paymentGateway,
            CommerceMetrics commerceMetrics
    ) {
        this.persistenceService =
                persistenceService;

        this.paymentGateway =
                paymentGateway;

        this.commerceMetrics = commerceMetrics;
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

            commerceMetrics
                    .recordPaymentResult(
                            "replayed"
                    );

            return replayCompleted(
                    prepared
            );
        }

        PaymentGatewayResult result;

        Instant gatewayStartedAt =
                Instant.now();

        try {

            result =
                    paymentGateway.charge(
                            new PaymentGatewayCommand(
                                    key,
                                    prepared.orderNumber(),
                                    prepared.amount()
                            )
                    );

            commerceMetrics
                    .recordPaymentGatewayDuration(
                            result.successful()
                                    ? "success"
                                    : "declined",
                            Duration.between(
                                    gatewayStartedAt,
                                    Instant.now()
                            )
                    );

        } catch (PaymentGatewayException exception) {

            log.atWarn()
                    .addKeyValue(
                            "orderId",
                            prepared.orderId()
                    )
                    .addKeyValue(
                            "paymentAttemptId",
                            prepared.paymentAttemptId()
                    )
                    .addKeyValue(
                            "result",
                            "unknown"
                    )
                    .log(
                            "Payment result is unknown"
                    );

            commerceMetrics
                    .recordPaymentGatewayDuration(
                            "unknown",
                            Duration.between(
                                    gatewayStartedAt,
                                    Instant.now()
                            )
                    );

            commerceMetrics
                    .recordPaymentResult(
                            "unknown"
                    );

            persistenceService.markUnknown(
                    prepared.paymentAttemptId(),
                    exception.getMessage()
            );

            throw new PaymentGatewayUnavailableException(
                    "Payment result is currently unknown. Retry this request using the same Idempotency-Key."
            );
        }

        if (result.successful()) {

            PaymentResponse response =
                    persistenceService
                            .markSucceeded(
                                    prepared.paymentAttemptId(),
                                    result.gatewayReference()
                            );

            log.atInfo()
                    .addKeyValue(
                            "orderId",
                            prepared.orderId()
                    )
                    .addKeyValue(
                            "paymentAttemptId",
                            prepared.paymentAttemptId()
                    )
                    .addKeyValue(
                            "result",
                            "success"
                    )
                    .log(
                            "Payment completed"
                    );

            commerceMetrics
                    .recordPaymentResult(
                            "success"
                    );

            return response;
        }

        PaymentResponse response =
                persistenceService
                        .markDeclinedAndRestoreStock(
                                prepared.paymentAttemptId(),
                                result.failureCode(),
                                result.failureMessage()
                        );

        log.atWarn()
                .addKeyValue(
                        "orderId",
                        prepared.orderId()
                )
                .addKeyValue(
                        "paymentAttemptId",
                        prepared.paymentAttemptId()
                )
                .addKeyValue(
                        "result",
                        "declined"
                )
                .log(
                        "Payment declined"
                );

        commerceMetrics
                .recordPaymentResult(
                        "declined"
                );

        return response;
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