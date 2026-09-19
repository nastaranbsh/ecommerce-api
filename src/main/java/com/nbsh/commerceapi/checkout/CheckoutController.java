package com.nbsh.commerceapi.checkout;

import com.nbsh.commerceapi.order.dto.OrderResponse;
import com.nbsh.commerceapi.security.CurrentUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/me/checkout")
public class CheckoutController {

    private final CheckoutService checkoutService;
    private final CurrentUser currentUser;

    public CheckoutController(
            CheckoutService checkoutService,
            CurrentUser currentUser
    ) {
        this.checkoutService =
                checkoutService;

        this.currentUser =
                currentUser;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> checkout(
            @AuthenticationPrincipal Jwt jwt
    ) {

        Long userId =
                currentUser.getUserId(jwt);

        OrderResponse order =
                checkoutService.checkout(
                        userId
                );

        return ResponseEntity
//                .status(HttpStatus.CREATED)
                .created(
                        URI.create(
                                "/api/v1/me/orders/"
                                        + order.id()
                        )
                )
                .body(order);
    }
}