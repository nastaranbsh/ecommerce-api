package com.nbsh.commerceapi.order;

import com.nbsh.commerceapi.common.exception.InvalidOrderStateException;
import com.nbsh.commerceapi.user.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class OrderTest {

    @Test
    void pendingCanBecomePaid() {
        var order = createOrder();
        order.changeStatus(OrderStatus.PAID);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    void deliveredCannotReturnToPending() {
        var order = createOrder();
        for (var status : new OrderStatus[]{OrderStatus.PAID, OrderStatus.PROCESSING, OrderStatus.SHIPPED, OrderStatus.DELIVERED}) {
            order.changeStatus(status);
        }
        assertThatThrownBy(() -> order.changeStatus(OrderStatus.PENDING)).isInstanceOf(InvalidOrderStateException.class);
    }

    @Test
    void cancelledIsTerminal() {
        var order = createOrder();
        order.changeStatus(OrderStatus.CANCELLED);
        assertThatThrownBy(() -> order.changeStatus(OrderStatus.PAID)).isInstanceOf(InvalidOrderStateException.class);
    }

    @Test
    void paymentFailureCannotBecomePaid() {
        var order = createOrder();
        order.changeStatus(OrderStatus.PAYMENT_FAILED);
        assertThatThrownBy(() -> order.changeStatus(OrderStatus.PAID)).isInstanceOf(InvalidOrderStateException.class);
    }

    private Order createOrder() {
        return new Order(
                "ORD-TEST",
                new User(
                        "a@test.com",
                        "hash",
                        "A",
                        "B",
                        UserRole.CUSTOMER,
                        true
                ),
                BigDecimal.TEN,
                BigDecimal.TEN
        );
    }
}
