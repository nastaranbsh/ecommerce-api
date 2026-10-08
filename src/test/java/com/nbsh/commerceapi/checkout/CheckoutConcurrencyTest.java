package com.nbsh.commerceapi.checkout;

import com.nbsh.commerceapi.support.*;
import com.nbsh.commerceapi.cart.*;
import com.nbsh.commerceapi.inventory.*;
import com.nbsh.commerceapi.order.*;
import com.nbsh.commerceapi.common.exception.InsufficientStockException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.*;

class CheckoutConcurrencyTest extends PostgresIntegrationTest {

    @Autowired
    TestDataFactory dataFactory;

    @Autowired
    CheckoutService checkoutService;

    @Autowired
    InventoryRepository inventoryRepository;

    @Autowired
    CartItemRepository cartItemRepository;

    @Autowired
    OrderRepository orderRepository;

    @Test
    void onlyOneCheckoutSucceedsForLastInventoryUnit() throws Exception {
        var product = dataFactory.createProduct(
                "Last unit",
                "LAST-1",
                "10.00",
                1
        );
        var user1 = dataFactory.createCustomer("user1@race.test");
        var user2 = dataFactory.createCustomer("user2@race.test");

        dataFactory.addToCart(
                dataFactory.createCart(user1),
                product,
                1
        );

        dataFactory.addToCart(
                dataFactory.createCart(user2),
                product,
                1
        );

        ExecutorService pool =
                Executors.newFixedThreadPool(2);

        var ready = new CountDownLatch(2);
        var start = new CountDownLatch(1);

        try {
            Callable<Boolean> one = () -> {
                ready.countDown();

                if (!start.await(
                        10,
                        TimeUnit.SECONDS
                )) {
                    throw new AssertionError("start timeout");
                }

                try {
                    checkoutService.checkout(user1.getId());
                    return true;
                } catch (InsufficientStockException ex) {
                    return false;
                }
            };

            Callable<Boolean> two = () -> {
                ready.countDown();

                if (!start.await(
                        10,
                        TimeUnit.SECONDS
                )) {
                    throw new AssertionError("start timeout");
                }

                try {
                    checkoutService.checkout(user2.getId());
                    return true;
                } catch (InsufficientStockException ex) {
                    return false;
                }
            };

            var f1 = pool.submit(one);
            var f2 = pool.submit(two);

            assertThat(ready.await(
                    10,
                    TimeUnit.SECONDS
            )).isTrue();

            start.countDown();

            assertThat((
                    f1.get(
                            30,
                            TimeUnit.SECONDS
                    ) ? 1 : 0
            ) + (
                    f2.get(
                            30,
                            TimeUnit.SECONDS
                    ) ? 1 : 0
            )).isEqualTo(1);

            assertThat(inventoryRepository
                    .findByProductId(product.getId())
                    .orElseThrow()
                    .getQuantityAvailable()).isZero();

            assertThat(orderRepository.count()).isEqualTo(1);

            assertThat(cartItemRepository.count()).isEqualTo(1);
        } finally {
            start.countDown();
            pool.shutdownNow();
        }
    }
}