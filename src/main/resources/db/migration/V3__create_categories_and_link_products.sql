CREATE TABLE categories
(
    id          BIGSERIAL PRIMARY KEY,

    name        VARCHAR(120) NOT NULL,

    slug        VARCHAR(120) NOT NULL,

    description TEXT,

    active      BOOLEAN NOT NULL DEFAULT TRUE,

    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_categories_name UNIQUE (name),

    CONSTRAINT uk_categories_slug UNIQUE (slug)
);


INSERT INTO categories (
    name,
    slug,
    description,
    active
)
VALUES (
           'Uncategorized',
           'uncategorized',
           'Default category for existing products',
           TRUE
       );


ALTER TABLE products
    ADD COLUMN category_id BIGINT;


UPDATE products
SET category_id = (
    SELECT id
    FROM categories
    WHERE slug = 'uncategorized'
);


ALTER TABLE products
    ALTER COLUMN category_id SET NOT NULL;


ALTER TABLE products
    ADD CONSTRAINT fk_products_category
        FOREIGN KEY (category_id)
            REFERENCES categories(id)
            ON DELETE RESTRICT;


CREATE INDEX idx_products_category_id
    ON products(category_id);