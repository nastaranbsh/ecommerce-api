package com.nbsh.commerceapi.common.exception;

public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }
}