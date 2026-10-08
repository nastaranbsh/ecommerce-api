package com.nbsh.commerceapi.order;

import com.nbsh.commerceapi.support.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderIntegrationTest extends ApiIntegrationTest {

    @Autowired
    TestDataFactory dataFactory;

    @Autowired
    MockMvc mockMvc;

    @Test
    void customerCannotReadAnotherCustomersOrder() throws Exception {
        var user1 = dataFactory.createCustomer("user1@orders.test");
        var user2 = dataFactory.createCustomer("user2@orders.test");

        var product = dataFactory.createProduct(
                "Item",
                "ORDER-OWN",
                "10.00",
                2
        );

        var order = dataFactory.createOrder(
                user1,
                product,
                OrderStatus.PENDING
        );

        mockMvc
                .perform(get(
                        "/api/v1/me/orders/{id}",
                        order.getId()
                ).with(jwt().jwt(
                        token -> token
                                .subject(user2
                                        .getId()
                                        .toString())
                                .claim(
                                        "userId",
                                        user2.getId()
                                ))))
                .andExpect(status().isNotFound());

        mockMvc
                .perform(get(
                        "/api/v1/admin/orders/{id}",
                        order.getId()
                ).with(
                        jwt().authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"))))
                .andExpect(status().isForbidden());
    }
}
