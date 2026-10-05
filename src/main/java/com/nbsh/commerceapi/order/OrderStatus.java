package com.nbsh.commerceapi.order;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = """
                Current order lifecycle state.

                PENDING: checkout created, awaiting payment
                PAID: payment succeeded
                PAYMENT_FAILED: definitive payment failure
                PROCESSING: fulfillment in progress
                SHIPPED: dispatched
                DELIVERED: delivered
                CANCELLED: cancelled
                REFUNDED: payment refunded
                """
)
public enum OrderStatus {

    PENDING,
    PAID,
    PAYMENT_FAILED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    REFUNDED
}