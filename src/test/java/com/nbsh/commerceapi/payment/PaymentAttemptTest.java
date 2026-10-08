package com.nbsh.commerceapi.payment;

import com.nbsh.commerceapi.order.Order;
import com.nbsh.commerceapi.user.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class PaymentAttemptTest {

    @Test
    void initiatedCanSucceed() {
        var paymentAttempt = paymentAttempt();


        paymentAttempt.markSucceeded("REF-1");

        assertThat(paymentAttempt.getStatus()).isEqualTo(PaymentStatus.SUCCEEDED);
        assertThat(paymentAttempt.getGatewayReference()).isEqualTo("REF-1");
    }

    @Test
    void initiatedCanDecline() {
        var paymentAttempt = paymentAttempt();

        paymentAttempt.markDeclined(
                "DECLINED",
                "Card declined"
        );

        assertThat(paymentAttempt.getStatus()).isEqualTo(PaymentStatus.DECLINED);
    }

    @Test
    void initiatedCanBecomeUnknown() {
        var paymentAttempt = paymentAttempt();

        paymentAttempt.markUnknown("Timeout");

        assertThat(paymentAttempt.getStatus()).isEqualTo(PaymentStatus.UNKNOWN);
    }

    private PaymentAttempt paymentAttempt() {
        var user = new User(
                "x@test.com",
                "hash",
                "X",
                "Y",
                UserRole.CUSTOMER,
                true
        );
        return new PaymentAttempt(
                new Order(
                        "ORD-X",
                        user,
                        BigDecimal.TEN,
                        BigDecimal.TEN
                ),
                BigDecimal.TEN,
                "unique-key-123"
        );
    }
}
