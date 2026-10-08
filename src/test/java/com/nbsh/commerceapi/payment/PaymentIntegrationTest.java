package com.nbsh.commerceapi.payment;

import com.nbsh.commerceapi.support.*;
import com.nbsh.commerceapi.payment.gateway.*;
import com.nbsh.commerceapi.order.*;
import com.nbsh.commerceapi.common.exception.PaymentGatewayUnavailableException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

class PaymentIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    TestDataFactory dataFactory;

    @Autowired
    PaymentService paymentService;

    @Autowired
    PaymentAttemptRepository paymentAttemptRepository;

    @Autowired
    OrderRepository orderRepository;

    @MockitoBean
    PaymentGateway paymentGateway;

    @Test
    void sameIdempotencyKeyDoesNotChargeTwice() {
        var user = dataFactory.createCustomer("pay@test.com");

        var product = dataFactory.createProduct(
                "Item",
                "PAY-1",
                "100.00",
                2
        );

        var order = dataFactory.createOrder(
                user,
                product,
                OrderStatus.PENDING
        );

        when(paymentGateway.charge(any())).thenReturn(PaymentGatewayResult.success("REF-100"));

        var firstPayment = paymentService.pay(
                user.getId(),
                order.getId(),
                "same-key-123"
        );

        var secondPayment = paymentService.pay(
                user.getId(),
                order.getId(),
                "same-key-123"
        );

        assertThat(secondPayment.replayed()).isTrue();
        assertThat(secondPayment.paymentAttemptId()).isEqualTo(firstPayment.paymentAttemptId());
        assertThat(paymentAttemptRepository.count()).isEqualTo(1);
        assertThat(orderRepository
                .findById(order.getId())
                .orElseThrow()
                .getStatus()).isEqualTo(OrderStatus.PAID);

        verify(
                paymentGateway,
                times(1)
        ).charge(any());
    }

    @Test
    void timeoutCanRecoverWithSameKey() {
        var user = dataFactory.createCustomer("timeout@test.com");

        var product = dataFactory.createProduct(
                "Item",
                "PAY-TIMEOUT",
                "100.00",
                2
        );

        var order = dataFactory.createOrder(
                user,
                product,
                OrderStatus.PENDING
        );

        when(paymentGateway.charge(any()))
                .thenThrow(new PaymentGatewayException("timeout"))
                .thenReturn(PaymentGatewayResult.success("REF-RECOVER"));

        assertThatThrownBy(() -> paymentService.pay(
                user.getId(),
                order.getId(),
                "timeout-key-123"
        )).isInstanceOf(PaymentGatewayUnavailableException.class);

        assertThat(paymentAttemptRepository
                .findByIdempotencyKey("timeout-key-123")
                .orElseThrow()
                .getStatus()).isEqualTo(PaymentStatus.UNKNOWN);

        assertThat(orderRepository
                .findById(order.getId())
                .orElseThrow()
                .getStatus()).isEqualTo(OrderStatus.PENDING);

        var secondPayment = paymentService.pay(
                user.getId(),
                order.getId(),
                "timeout-key-123"
        );

        assertThat(secondPayment.status()).isEqualTo(PaymentStatus.SUCCEEDED);

        assertThat(paymentAttemptRepository.count()).isEqualTo(1);

        assertThat(orderRepository
                .findById(order.getId())
                .orElseThrow()
                .getStatus()).isEqualTo(OrderStatus.PAID);
    }
}
