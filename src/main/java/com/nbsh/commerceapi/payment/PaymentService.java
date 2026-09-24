package com.nbsh.commerceapi.payment;

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
            Long orderId
    ) {

        PaymentPersistenceService.PreparedPayment prepared =
                persistenceService
                        .preparePayment(
                                userId,
                                orderId
                        );

        PaymentGatewayResult result;

        try {

            result =
                    paymentGateway.charge(
                            new PaymentGatewayCommand(
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
                    "Payment result is currently unknown. Do not retry the payment yet."
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
}