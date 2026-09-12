CREATE INDEX idx_products_active
    ON products(active);

CREATE INDEX idx_products_price
    ON products(price);

CREATE INDEX idx_products_created_at
    ON products(created_at);