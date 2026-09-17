package com.nbsh.commerceapi.common.exception;

public class InsufficientStockException
        extends RuntimeException {

    public InsufficientStockException(
            String message
    ) {
        super(message);
    }
}