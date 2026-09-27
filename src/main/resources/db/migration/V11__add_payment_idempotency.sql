ALTER TABLE payment_attempts
    ADD COLUMN idempotency_key VARCHAR(100);


UPDATE payment_attempts
SET idempotency_key =
        'legacy-' || id
WHERE idempotency_key IS NULL;


ALTER TABLE payment_attempts
    ALTER COLUMN idempotency_key SET NOT NULL;


ALTER TABLE payment_attempts
    ADD CONSTRAINT uk_payment_attempts_idempotency_key
        UNIQUE (idempotency_key);