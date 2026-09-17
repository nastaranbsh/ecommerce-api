CREATE TABLE inventories
(
    id                 BIGSERIAL PRIMARY KEY,

    product_id         BIGINT NOT NULL,

    quantity_available INTEGER NOT NULL DEFAULT 0,

    version            BIGINT NOT NULL DEFAULT 0,

    created_at         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_inventories_product
        UNIQUE (product_id),

    CONSTRAINT fk_inventories_product
        FOREIGN KEY (product_id)
            REFERENCES products(id)
            ON DELETE CASCADE,

    CONSTRAINT chk_inventory_quantity_non_negative
        CHECK (quantity_available >= 0)
);


CREATE INDEX idx_inventories_product_id
    ON inventories(product_id);


INSERT INTO inventories (
    product_id,
    quantity_available,
    version
)
SELECT
    id,
    0,
    0
FROM products;


ALTER TABLE cart_items
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;