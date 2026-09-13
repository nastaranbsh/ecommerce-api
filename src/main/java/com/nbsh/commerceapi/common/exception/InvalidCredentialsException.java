package com.nbsh.commerceapi.common.exception;

public class InvalidCredentialsException
        extends RuntimeException {

    public InvalidCredentialsException(
            String message
    ) {
        super(message);
    }
}