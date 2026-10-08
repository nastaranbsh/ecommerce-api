package com.nbsh.commerceapi.product;

import com.nbsh.commerceapi.product.dto.CreateProductRequest;
import com.nbsh.commerceapi.product.dto.ProductResponse;
import com.nbsh.commerceapi.support.ApiIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

//@WebMvcTest(controllers = ProductController.class)
//@Import({
//        SecurityConfig.class,
//        RestAuthenticationEntryPoint.class,
//        RestAccessDeniedHandler.class
//})
//@ActiveProfiles("test")
class ProductIntegrationTest extends ApiIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ProductService productService;


    private static final String PRODUCTS_URL = "/api/v1/products";

    @Test
    void anonymousCannotCreateProduct() throws
            Exception {
        mockMvc
                .perform(post(PRODUCTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validProductJson(
                                "keyboard",
                                100,
                                "KB-1",
                                1
                        )))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void customerCannotCreateProduct() throws
            Exception {
        mockMvc
                .perform(post(PRODUCTS_URL)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validProductJson(
                                "keyboard",
                                100,
                                "KB-1",
                                1
                        )))
                .andExpect(status().isForbidden());
    }

//    @Test
//    void adminCanCreateProduct() throws
//            Exception {
//        mockMvc
//                .perform(post(PRODUCTS_URL)
//                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(validProductJson(
//                                "Keyboard",
//                                100,
//                                "KB-1",
//                                1
//                        )))
//                .andExpect(status().isCreated());
//    }

    @Test
    void adminCanCreateProduct() throws Exception {

        ProductResponse response =
                new ProductResponse(
                        10L,
                        "Keyboard",
                        "Test keyboard",
                        new BigDecimal("100.00"),
                        "KB-1",
                        true,
                        null,
                        Instant.now(),
                        Instant.now()
                );

        when(
                productService.createProduct(any(CreateProductRequest.class))
        ).thenReturn(response);

        mockMvc
                .perform(
                        post("/api/v1/products")
                                .with(
                                        jwt().authorities(
                                                new SimpleGrantedAuthority(
                                                        "ROLE_ADMIN"
                                                )
                                        )
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        validProductJson(
                                                "Keyboard",
                                                100,
                                                "KB-1",
                                                1
                                        )
                                )
                )
                .andExpect(
                        status().isCreated()
                )
                .andExpect(
                        header().string(
                                "Location",
                                "/api/v1/products/10"
                        )
                );
    }

    @Test
    void productPriceCannotBeNegative() throws
            Exception {

        mockMvc
                .perform(post(PRODUCTS_URL)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validProductJson(
                                "Keyboard",
                                -5,
                                "KB-1",
                                1
                        )))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").isArray());
    }

    private String validProductJson(
            String name,
            int price,
            String sku,
            long categoryId
    ) {
        return """
                {
                  "name": "%s",
                  "description": "Test",
                  "price": %d,
                  "sku": "%s",
                  "categoryId": %d
                }
                """.formatted(
                name,
                price,
                sku,
                categoryId
        );
    }

//    private String productJson(
//            String name,
//            BigDecimal price,
//            String sku,
//            Long categoryId
//    ) throws JsonProcessingException {
//
//        CreateProductRequest request = new CreateProductRequest(
//                name,
//                "Test",
//                price,
//                sku,
//                categoryId
//        );
//
//        return objectMapper.writeValueAsString(request);
//    }
}
