CREATE TABLE orders
(
    id              BIGSERIAL PRIMARY KEY,

    order_number    VARCHAR(50) NOT NULL,

    user_id         BIGINT NOT NULL,

    status          VARCHAR(50) NOT NULL,

    subtotal        NUMERIC(12, 2) NOT NULL,

    total_amount    NUMERIC(12, 2) NOT NULL,

    version         BIGINT NOT NULL DEFAULT 0,

    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_orders_order_number
        UNIQUE (order_number),

    CONSTRAINT fk_orders_user
        FOREIGN KEY (user_id)
            REFERENCES users(id)
            ON DELETE RESTRICT,

    CONSTRAINT chk_orders_subtotal_non_negative
        CHECK (subtotal >= 0),

    CONSTRAINT chk_orders_total_non_negative
        CHECK (total_amount >= 0),

    CONSTRAINT chk_orders_status
        CHECK (
            status IN (
                       'PENDING',
                       'PAID',
                       'PAYMENT_FAILED',
                       'PROCESSING',
                       'SHIPPED',
                       'DELIVERED',
                       'CANCELLED',
                       'REFUNDED'
                )
            )
);


CREATE TABLE order_items
(
    id              BIGSERIAL PRIMARY KEY,

    order_id        BIGINT NOT NULL,

    product_id      BIGINT,

    product_name    VARCHAR(150) NOT NULL,

    product_sku     VARCHAR(100) NOT NULL,

    unit_price      NUMERIC(12, 2) NOT NULL,

    quantity        INTEGER NOT NULL,

    line_total      NUMERIC(12, 2) NOT NULL,

    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id)
            REFERENCES orders(id)
            ON DELETE CASCADE,

    CONSTRAINT fk_order_items_product
        FOREIGN KEY (product_id)
            REFERENCES products(id)
            ON DELETE SET NULL,

    CONSTRAINT chk_order_items_unit_price_non_negative
        CHECK (unit_price >= 0),

    CONSTRAINT chk_order_items_quantity_positive
        CHECK (quantity > 0),

    CONSTRAINT chk_order_items_line_total_non_negative
        CHECK (line_total >= 0)
);


CREATE INDEX idx_orders_user_id
    ON orders(user_id);


CREATE INDEX idx_orders_status
    ON orders(status);


CREATE INDEX idx_orders_created_at
    ON orders(created_at);


CREATE INDEX idx_order_items_order_id
    ON order_items(order_id);


CREATE INDEX idx_order_items_product_id
    ON order_items(product_id);