CREATE TABLE products
(
    id          BIGSERIAL PRIMARY KEY,

    name        VARCHAR(150) NOT NULL,

    description TEXT,

    price       NUMERIC(12, 2) NOT NULL,

    sku         VARCHAR(100) NOT NULL,

    active      BOOLEAN NOT NULL DEFAULT TRUE,

    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_products_sku UNIQUE (sku),

    CONSTRAINT chk_products_price_non_negative
        CHECK (price >= 0)
);