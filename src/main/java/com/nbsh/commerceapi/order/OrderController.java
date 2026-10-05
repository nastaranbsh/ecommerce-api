package com.nbsh.commerceapi.order;

import com.nbsh.commerceapi.common.api.PageResponse;
import com.nbsh.commerceapi.config.OpenApiConfig;
import com.nbsh.commerceapi.order.dto.OrderResponse;
import com.nbsh.commerceapi.order.dto.OrderSummaryResponse;
import com.nbsh.commerceapi.security.CurrentUser;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RestController
@RequestMapping("/api/v1/me/orders")
public class OrderController {

    private static final int MAX_PAGE_SIZE = 100;

    private final OrderService orderService;
    private final CurrentUser currentUser;

    public OrderController(
            OrderService orderService,
            CurrentUser currentUser
    ) {
        this.orderService = orderService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public PageResponse<OrderSummaryResponse>
    getOrders(
            @AuthenticationPrincipal Jwt jwt,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "20")
            int size
    ) {

        validatePagination(
                page,
                size
        );

        Long userId =
                currentUser.getUserId(jwt);

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                Sort.Direction.DESC,
                                "createdAt"
                        )
                );

        return orderService
                .getOrdersForUser(
                        userId,
                        pageable
                );
    }

    @GetMapping("/{orderId}")
    public OrderResponse getOrder(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long orderId
    ) {

        Long userId =
                currentUser.getUserId(jwt);

        return orderService
                .getOrderForUser(
                        userId,
                        orderId
                );
    }

    private void validatePagination(
            int page,
            int size
    ) {

        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page number must be greater than or equal to 0"
            );
        }

        if (size < 1
                || size > MAX_PAGE_SIZE) {

            throw new IllegalArgumentException(
                    "Page size must be between 1 and "
                            + MAX_PAGE_SIZE
            );
        }
    }
}