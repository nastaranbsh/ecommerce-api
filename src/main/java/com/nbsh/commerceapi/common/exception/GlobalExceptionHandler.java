package com.nbsh.commerceapi.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request
    ) {

        ApiErrorResponse error = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                exception.getMessage(),
                request.getRequestURI(),
                List.of()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error);
    }

    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceConflict(
            ResourceConflictException exception,
            HttpServletRequest request
    ) {

        ApiErrorResponse error = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                exception.getMessage(),
                request.getRequestURI(),
                List.of()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {

        List<ValidationError> validationErrors =
                exception.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(fieldError ->
                                new ValidationError(
                                        fieldError.getField(),
                                        fieldError.getDefaultMessage()
                                )
                        )
                        .toList();

        ApiErrorResponse error = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Request validation failed",
                request.getRequestURI(),
                validationErrors
        );

        return ResponseEntity
                .badRequest()
                .body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request
    ) {

        ApiErrorResponse error = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Invalid value for parameter: " + exception.getName(),
                request.getRequestURI(),
                List.of()
        );

        return ResponseEntity
                .badRequest()
                .body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleMalformedJson(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {

        ApiErrorResponse error = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Malformed request body",
                request.getRequestURI(),
                List.of()
        );

        return ResponseEntity
                .badRequest()
                .body(error);
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidRequest(
            InvalidRequestException exception,
            HttpServletRequest request
    ) {

        ApiErrorResponse error = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                exception.getMessage(),
                request.getRequestURI(),
                List.of()
        );

        return ResponseEntity
                .badRequest()
                .body(error);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidCredentials(
            InvalidCredentialsException exception,
            HttpServletRequest request
    ) {

        ApiErrorResponse error = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                exception.getMessage(),
                request.getRequestURI(),
                List.of()
        );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(error);
    }

    @ExceptionHandler(
            InsufficientStockException.class
    )
    public ResponseEntity<ApiErrorResponse>
    handleInsufficientStock(
            InsufficientStockException exception,
            HttpServletRequest request
    ) {

        ApiErrorResponse error = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                exception.getMessage(),
                request.getRequestURI(),
                List.of()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }

    @ExceptionHandler(
            OptimisticLockingFailureException.class
    )
    public ResponseEntity<ApiErrorResponse>
    handleOptimisticLockingFailure(
            OptimisticLockingFailureException exception,
            HttpServletRequest request
    ) {

        ApiErrorResponse error = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                "The resource was modified by another request. Please retry.",
                request.getRequestURI(),
                List.of()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(
            Exception exception,
            HttpServletRequest request
    ) {

        ApiErrorResponse error = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "An unexpected error occurred",
                request.getRequestURI(),
                List.of()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }
}