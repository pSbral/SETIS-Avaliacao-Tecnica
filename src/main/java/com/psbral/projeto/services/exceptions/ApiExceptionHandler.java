package com.psbral.projeto.services.exceptions;

import com.psbral.projeto.services.exceptions.models.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class ApiExceptionHandler {

    // 400 - IllegalArgumentException
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        ApiError body = new ApiError(
                Instant.now(),
                status.value(),
                ex.getMessage(),
                "Bad Request",
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(body);
    }

    // 400 - NotValid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        String message = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .findFirst()
                .orElse("Erro de validação");

        ApiError body = new ApiError(
                Instant.now(),
                status.value(),
                message,
                "Validation error",
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(body);
    }

    // Fallback -> 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(
            Exception ex,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        ApiError body = new ApiError(
                Instant.now(),
                status.value(),
                "Unexpected error",
                "Internal Server Error",
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(body);
    }
}
