package com.nbsh.commerceapi.order.dto;

import com.nbsh.commerceapi.order.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(

        @NotNull(
                message = "Order status is required"
        )
        OrderStatus status

) {
}