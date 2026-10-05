package com.nbsh.commerceapi.payment;

import com.nbsh.commerceapi.config.OpenApiConfig;
import com.nbsh.commerceapi.payment.dto.PaymentResponse;
import com.nbsh.commerceapi.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RestController
@RequestMapping("/api/v1/me/orders")
public class PaymentController {

    private final PaymentService paymentService;
    private final CurrentUser currentUser;

    public PaymentController(
            PaymentService paymentService,
            CurrentUser currentUser
    ) {
        this.paymentService =
                paymentService;

        this.currentUser =
                currentUser;
    }

    @Operation(
            summary = "Pay for an order",
            description = """
                Attempts payment for a PENDING order.

                The operation is idempotent when the same Idempotency-Key
                is reused. A successful retry returns the existing logical
                payment result instead of creating another charge.

                A definitive decline results in PAYMENT_FAILED and restores
                inventory. An ambiguous gateway result keeps the order
                PENDING for safe retry with the same idempotency key.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Payment reached a definitive business result"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Idempotency-Key missing or invalid"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Order not found for authenticated user"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Order/payment state conflicts with request"
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "Payment outcome is currently unknown"
            )
    })
    @PostMapping("/{orderId}/payment")
    public PaymentResponse pay(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long orderId,
            @Parameter(
                    description = """
                        Unique identifier for one logical payment operation.

                        Reuse the same key when retrying after a timeout or
                        ambiguous network failure. Do not reuse a key for a
                        different order.
                        """,
                    required = true,
                    example = "7df82049-1e22-48c3-a979-113510b1214b"
            )
            @RequestHeader(
                    value = "Idempotency-Key",
                    required = false
            )
            String idempotencyKey
    ) {

        Long userId =
                currentUser.getUserId(jwt);

        return paymentService.pay(
                userId,
                orderId,
                idempotencyKey
        );
    }
}