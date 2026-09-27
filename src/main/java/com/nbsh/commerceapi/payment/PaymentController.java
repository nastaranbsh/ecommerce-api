package com.nbsh.commerceapi.payment;

import com.nbsh.commerceapi.payment.dto.PaymentResponse;
import com.nbsh.commerceapi.security.CurrentUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/{orderId}/payment")
    public PaymentResponse pay(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long orderId,
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