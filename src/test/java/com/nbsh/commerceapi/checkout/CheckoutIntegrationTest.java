package com.nbsh.commerceapi.checkout;

import com.nbsh.commerceapi.support.*;
import com.nbsh.commerceapi.cart.*;
import com.nbsh.commerceapi.inventory.*;
import com.nbsh.commerceapi.order.*;
import com.nbsh.commerceapi.product.*;
import com.nbsh.commerceapi.common.exception.InsufficientStockException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.*;

class CheckoutIntegrationTest extends PostgresIntegrationTest {

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

    @Autowired
    ProductRepository productRepository;

    @Test
    void checkoutRollsBackWhenOneItemIsOutOfStock() {
        var user = dataFactory.createCustomer("rollback@test.com");

        var keyboard = dataFactory.createProduct(
                "Keyboard",
                "KB-ROLL",
                "100.00",
                2
        );

        var mouse = dataFactory.createProduct(
                "Mouse",
                "MS-ROLL",
                "50.00",
                0
        );

        var cart = dataFactory.createCart(user);

        dataFactory.addToCart(
                cart,
                keyboard,
                1
        );
        dataFactory.addToCart(
                cart,
                mouse,
                1
        );

        assertThatThrownBy(() -> checkoutService.checkout(user.getId())).isInstanceOf(InsufficientStockException.class);

        assertThat(inventoryRepository
                .findByProductId(keyboard.getId())
                .orElseThrow()
                .getQuantityAvailable()).isEqualTo(2);

        assertThat(inventoryRepository
                .findByProductId(mouse.getId())
                .orElseThrow()
                .getQuantityAvailable()).isZero();

        assertThat(cartItemRepository
                .findAll()
                .stream()
                .filter(i -> i
                        .getCart()
                        .getId()
                        .equals(cart.getId()))).hasSize(2);

        assertThat(orderRepository
                .findAllByUserId(
                        user.getId(),
                        PageRequest.of(
                                0,
                                10
                        )
                )
                .getTotalElements()).isZero();
    }

    @Test
    void successfulCheckoutPreservesHistoricalPrice() {
        var user = dataFactory.createCustomer("success@test.com");

        var product = dataFactory.createProduct(
                "Keyboard",
                "KB-SUCCESS",
                "100.00",
                5
        );

        var cart = dataFactory.createCart(user);

        dataFactory.addToCart(
                cart,
                product,
                2
        );

        var result = checkoutService.checkout(user.getId());

        var order = orderRepository
                .findWithItemsById(result.id())
                .orElseThrow();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);

        assertThat(order.getTotalAmount()).isEqualByComparingTo("200.00");

        assertThat(order
                .getItems()
                .getFirst()
                .getUnitPrice()).isEqualByComparingTo("100.00");

        assertThat(inventoryRepository
                .findByProductId(product.getId())
                .orElseThrow()
                .getQuantityAvailable()).isEqualTo(3);

        assertThat(cartItemRepository.findAll()).isEmpty();

        product = productRepository
                .findById(product.getId())
                .orElseThrow();

        product.setPrice(new java.math.BigDecimal("150.00"));

        productRepository.saveAndFlush(product);

        assertThat(orderRepository
                .findWithItemsById(order.getId())
                .orElseThrow()
                .getItems()
                .getFirst()
                .getUnitPrice()).isEqualByComparingTo("100.00");
    }
}
