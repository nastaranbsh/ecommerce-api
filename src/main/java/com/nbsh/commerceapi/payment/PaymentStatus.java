package com.nbsh.commerceapi.payment;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = """
                Payment attempt state.

                INITIATED: recorded but not finalized
                SUCCEEDED: definitive payment success
                DECLINED: definitive provider decline
                UNKNOWN: transport failure left provider outcome uncertain
                """
)
public enum PaymentStatus {

    INITIATED,
    SUCCEEDED,
    DECLINED,
    UNKNOWN
}