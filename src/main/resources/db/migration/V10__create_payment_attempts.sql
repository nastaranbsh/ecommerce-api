CREATE TABLE payment_attempts
(
    id                BIGSERIAL PRIMARY KEY,

    order_id          BIGINT NOT NULL,

    amount            NUMERIC(12, 2) NOT NULL,

    status            VARCHAR(50) NOT NULL,

    gateway_reference VARCHAR(100),

    failure_code      VARCHAR(100),

    failure_message   VARCHAR(500),

    version           BIGINT NOT NULL DEFAULT 0,

    created_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_payment_attempts_order
        FOREIGN KEY (order_id)
            REFERENCES orders(id)
            ON DELETE RESTRICT,

    CONSTRAINT chk_payment_attempts_amount_non_negative
        CHECK (amount >= 0),

    CONSTRAINT chk_payment_attempts_status
        CHECK (
            status IN (
                       'INITIATED',
                       'SUCCEEDED',
                       'DECLINED',
                       'UNKNOWN'
                )
            )
);


CREATE INDEX idx_payment_attempts_order_id
    ON payment_attempts(order_id);


CREATE INDEX idx_payment_attempts_status
    ON payment_attempts(status);


CREATE UNIQUE INDEX uk_payment_attempts_gateway_reference
    ON payment_attempts(gateway_reference)
    WHERE gateway_reference IS NOT NULL;