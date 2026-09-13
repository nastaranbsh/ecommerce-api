CREATE TABLE users
(
    id            BIGSERIAL PRIMARY KEY,

    email         VARCHAR(320) NOT NULL,

    password_hash VARCHAR(255) NOT NULL,

    first_name    VARCHAR(100) NOT NULL,

    last_name     VARCHAR(100) NOT NULL,

    role          VARCHAR(50) NOT NULL,

    active        BOOLEAN NOT NULL DEFAULT TRUE,

    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_users_email UNIQUE (email),

    CONSTRAINT chk_users_role
        CHECK (role IN ('CUSTOMER', 'ADMIN'))
);


CREATE TABLE addresses
(
    id                BIGSERIAL PRIMARY KEY,

    user_id           BIGINT NOT NULL,

    label             VARCHAR(100),

    recipient_name    VARCHAR(200) NOT NULL,

    line1             VARCHAR(255) NOT NULL,

    line2             VARCHAR(255),

    city              VARCHAR(150) NOT NULL,

    state_or_province VARCHAR(150),

    postal_code       VARCHAR(50) NOT NULL,

    country_code      VARCHAR(2) NOT NULL,

    phone             VARCHAR(50),

    default_address   BOOLEAN NOT NULL DEFAULT FALSE,

    created_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_addresses_user
        FOREIGN KEY (user_id)
            REFERENCES users(id)
            ON DELETE CASCADE
);


CREATE INDEX idx_addresses_user_id
    ON addresses(user_id);


CREATE INDEX idx_users_active
    ON users(active);


CREATE INDEX idx_users_role
    ON users(role);