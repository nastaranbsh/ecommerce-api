package com.nbsh.commerceapi.checkout;

import com.nbsh.commerceapi.config.OpenApiConfig;
import com.nbsh.commerceapi.order.dto.OrderResponse;
import com.nbsh.commerceapi.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@Tag(
        name = "Checkout",
        description = "Checkout user's cart"
)
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

    @Operation(
            summary = "Checkout current cart",
            description = """
                Atomically converts the authenticated user's current cart
                into a PENDING order.

                Checkout revalidates product availability and current prices,
                pessimistically locks required inventory rows, decrements
                stock, creates historical OrderItems, and clears the cart.

                No client-provided price or user identifier is accepted.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Checkout succeeded and order was created"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Cart is empty or contains an unavailable product"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Insufficient stock or concurrency conflict"
            )
    })
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