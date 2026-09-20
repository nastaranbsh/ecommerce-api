package com.nbsh.commerceapi.common.exception;

public class InvalidOrderStateException
        extends RuntimeException {

    public InvalidOrderStateException(
            String message
    ) {
        super(message);
    }
}