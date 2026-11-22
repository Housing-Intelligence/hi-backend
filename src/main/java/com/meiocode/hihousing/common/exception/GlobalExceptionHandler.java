
package com.meiocode.hihousing.common.exception;

import com.meiocode.hihousing.common.api.ApiResponse;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Objects;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
        LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Handle business exceptions with an explicit HTTP status
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleResponseStatusException(
            ResponseStatusException ex
    ) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());

        String message = ex.getReason() != null
                ? ex.getReason()
                : status.getReasonPhrase();

        return ResponseEntity
                .status(status)
                .body(ApiResponse.failure(message));
    }

    // Handle @Valid request validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException ex
    ) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": "
                        + Objects.requireNonNullElse(
                                error.getDefaultMessage(),
                                "Invalid value"
                        ))
                .findFirst()
                .orElse("Request validation failed");

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.failure(message));
    }

    // Handle invalid JSON request body
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidRequestBody(
            HttpMessageNotReadableException ex
    ) {
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.failure("Invalid request body"));
    }

    // Handle database constraint violations
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(
            DataIntegrityViolationException ex
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure(
                        "The request conflicts with existing data"
                ));
    }

    // Handle unexpected exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(
            Exception ex
    ) {
        // Log the exception for debugging.
        log.error("Unexpected server error", ex);
        // Do not expose internal exception details to API clients.
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.failure(
                        "An unexpected error occurred"
                ));
    }
}
