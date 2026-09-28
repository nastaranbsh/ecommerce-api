package com.nbsh.commerceapi.common.exception;

public class ReviewNotAllowedException
        extends RuntimeException {

    public ReviewNotAllowedException(
            String message
    ) {
        super(message);
    }
}