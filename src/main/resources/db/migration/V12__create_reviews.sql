CREATE TABLE reviews
(
    id         BIGSERIAL PRIMARY KEY,

    user_id    BIGINT NOT NULL,

    product_id BIGINT NOT NULL,

    rating     INTEGER NOT NULL,

    title      VARCHAR(150),

    comment    VARCHAR(5000) NOT NULL,

    version    BIGINT NOT NULL DEFAULT 0,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_reviews_user
        FOREIGN KEY (user_id)
            REFERENCES users(id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_reviews_product
        FOREIGN KEY (product_id)
            REFERENCES products(id)
            ON DELETE CASCADE,

    CONSTRAINT uk_reviews_user_product
        UNIQUE (user_id, product_id),

    CONSTRAINT chk_reviews_rating
        CHECK (rating BETWEEN 1 AND 5)
);


CREATE INDEX idx_reviews_product_id
    ON reviews(product_id);


CREATE INDEX idx_reviews_user_id
    ON reviews(user_id);


CREATE INDEX idx_reviews_created_at
    ON reviews(created_at);