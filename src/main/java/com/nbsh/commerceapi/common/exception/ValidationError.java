package com.nbsh.commerceapi.common.exception;

public record ValidationError(
        String field,
        String message
) {
}