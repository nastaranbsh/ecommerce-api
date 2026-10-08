package com.nbsh.commerceapi.cart;

import com.nbsh.commerceapi.support.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.*;

class CartIntegrationTest extends ApiIntegrationTest {

    @Autowired
    TestDataFactory dataFactory;

    @Autowired
    CartItemRepository cartItemRepository;

    @Autowired
    MockMvc mockMvc;

    @Test
    void customerCannotDeleteAnotherCustomerCartItem() throws Exception {
        var user1 = dataFactory.createCustomer("user1@cart.test");
        var user2 = dataFactory.createCustomer("user2@cart.test");

        var product = dataFactory.createProduct(
                "Item",
                "CART-OWN",
                "10.00",
                2
        );

        var user1Cart =
                dataFactory.createCart(user1);

        dataFactory.createCart(user2);

        var user1CartItem = dataFactory.addToCart(
                user1Cart,
                product,
                1
        );

        mockMvc
                .perform(delete(
                        "/api/v1/me/cart/items/{id}",
                        user1CartItem.getId()
                ).with(jwt()
                        .jwt(
                                token -> token
                                        .subject(user2
                                                .getId()
                                                .toString())
                                        .claim(
                                                "userId",
                                                user2.getId()
                                        )
                        )
                        .authorities(
                                new SimpleGrantedAuthority(
                                        "ROLE_CUSTOMER"
                                )
                        )
                ))
                .andExpect(status().isNotFound());

        assertThat(cartItemRepository.existsById(user1CartItem.getId())).isTrue();
    }
}
