package com.nbsh.commerceapi.payment;

import com.nbsh.commerceapi.common.exception.*;
import com.nbsh.commerceapi.observability.CommerceMetrics;
import com.nbsh.commerceapi.payment.gateway.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentServiceTest {

    PaymentPersistenceService paymentPersistenceService = mock(PaymentPersistenceService.class);
    PaymentGateway paymentGateway = mock(PaymentGateway.class);
    CommerceMetrics commerceMetrics = mock(CommerceMetrics.class);

    PaymentService paymentService = new PaymentService(
            paymentPersistenceService,
            paymentGateway,
            commerceMetrics
    );

    @Test
    void succeededPaymentIsReplayedWithoutCallingGateway() {
        when(paymentPersistenceService.preparePayment(
                1L,
                20L,
                "key-12345678"
        )).thenReturn(prepared(PaymentStatus.SUCCEEDED));

        var response = paymentService.pay(
                1L,
                20L,
                "key-12345678"
        );

        assertThat(response.replayed()).isTrue();

        assertThat(response.gatewayReference()).isEqualTo("REF-1");

        verifyNoInteractions(paymentGateway);
    }

    @Test
    void successfulGatewayMarksSucceeded() {
        when(paymentPersistenceService.preparePayment(
                1L,
                20L,
                "key-12345678"
        )).thenReturn(prepared(PaymentStatus.INITIATED));

        when(paymentGateway.charge(any())).thenReturn(PaymentGatewayResult.success("REF-2"));

        paymentService.pay(
                1L,
                20L,
                "key-12345678"
        );

        verify(paymentPersistenceService).markSucceeded(
                10L,
                "REF-2"
        );

        verify(paymentPersistenceService, never())
                .markDeclinedAndRestoreStock(anyLong(), anyString(), anyString());
    }

    @Test
    void declinedGatewayRestoresStock() {
        when(paymentPersistenceService.preparePayment(
                1L,
                20L,
                "key-12345678"
        )).thenReturn(prepared(PaymentStatus.INITIATED));

        when(paymentGateway.charge(any()
        )).thenReturn(PaymentGatewayResult.declined(
                "CARD",
                "Declined"
        ));

        paymentService.pay(
                1L,
                20L,
                "key-12345678"
        );

        verify(paymentPersistenceService).markDeclinedAndRestoreStock(
                10L,
                "CARD",
                "Declined"
        );

        verify(
                paymentPersistenceService,
                never()
        ).markSucceeded(
                anyLong(),
                anyString()
        );
    }

    @Test
    void gatewayTimeoutIsUnknownNotDeclined() {
        when(paymentPersistenceService.preparePayment(
                1L,
                20L,
                "key-12345678"
        )).thenReturn(prepared(PaymentStatus.INITIATED));

        when(paymentGateway.charge(any())).thenThrow(new PaymentGatewayException("timeout"));

        assertThatThrownBy(() -> paymentService.pay(
                1L,
                20L,
                "key-12345678"
        )).isInstanceOf(PaymentGatewayUnavailableException.class);

        verify(paymentPersistenceService).markUnknown(
                10L,
                "timeout"
        );

        verify(
                paymentPersistenceService,
                never()
        ).markSucceeded(
                anyLong(),
                anyString()
        );

        verify(
                paymentPersistenceService,
                never()
        ).markDeclinedAndRestoreStock(
                anyLong(),
                anyString(),
                anyString()
        );
    }

    @Test
    void shortIdempotencyKeyIsRejected() {
        assertThatThrownBy(() -> paymentService.pay(
                1L,
                20L,
                "short"
        )).isInstanceOf(InvalidRequestException.class);

        verifyNoInteractions(
                paymentGateway,
                paymentPersistenceService
        );
    }

    private PaymentPersistenceService.PreparedPayment prepared(PaymentStatus status) {
        return new PaymentPersistenceService.PreparedPayment(
                10L,
                20L,
                "ORD-20",
                new BigDecimal("100.00"),
                status,
                "REF-1",
                null,
                null
        );
    }
}
