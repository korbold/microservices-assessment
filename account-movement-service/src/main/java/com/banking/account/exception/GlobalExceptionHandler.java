package com.banking.account.exception;

import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for Account-Movement Service
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @Value("${spring.profiles.active:local}")
    private String activeProfile;
    
    private boolean isProduction() {
        return "prod".equals(activeProfile) || "production".equals(activeProfile);
    }

    /**
     * Handle validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = new ErrorResponse(
            "Validation failed",
            errors.toString(),
            HttpStatus.BAD_REQUEST.value(),
            LocalDateTime.now()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle illegal argument exceptions
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            ex.getMessage(),
            request.getDescription(false),
            HttpStatus.BAD_REQUEST.value(),
            LocalDateTime.now()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle insufficient balance exceptions
     */
    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientBalanceException(
            InsufficientBalanceException ex, WebRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            "Saldo no disponible",
            ex.getMessage(),
            HttpStatus.BAD_REQUEST.value(),
            LocalDateTime.now()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle Feign client exceptions
     */
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponse> handleFeignException(
            FeignException ex, WebRequest request) {
        
        logger.error("Feign client error occurred: status={}, message={}", ex.status(), ex.getMessage());
        
        HttpStatus httpStatus;
        String message;
        
        if (ex.status() == 404) {
            httpStatus = HttpStatus.NOT_FOUND;
            message = "Resource not found in external service";
        } else if (ex.status() >= 500) {
            httpStatus = HttpStatus.BAD_GATEWAY;
            message = "External service error";
        } else if (ex.status() == 503) {
            httpStatus = HttpStatus.SERVICE_UNAVAILABLE;
            message = "External service temporarily unavailable";
        } else {
            httpStatus = HttpStatus.BAD_GATEWAY;
            message = "Error communicating with external service";
        }
        
        String errorDetails = isProduction() 
            ? message 
            : String.format("%s: %s", message, ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            message,
            errorDetails,
            httpStatus.value(),
            LocalDateTime.now()
        );

        return new ResponseEntity<>(errorResponse, httpStatus);
    }

    /**
     * Handle generic exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, WebRequest request) {
        
        // Log full stack trace for debugging
        logger.error("Internal server error occurred", ex);
        
        // In production, hide technical details
        String errorDetails = isProduction() 
            ? "An unexpected error occurred. Please contact support." 
            : ex.getMessage();
        
        ErrorResponse errorResponse = new ErrorResponse(
            "Internal server error",
            errorDetails,
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            LocalDateTime.now()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Custom exception for insufficient balance
     */
    public static class InsufficientBalanceException extends RuntimeException {
        public InsufficientBalanceException(String message) {
            super(message);
        }
    }

    /**
     * Error response class
     */
    public static class ErrorResponse {
        private String message;
        private String details;
        private int status;
        private LocalDateTime timestamp;

        public ErrorResponse(String message, String details, int status, LocalDateTime timestamp) {
            this.message = message;
            this.details = details;
            this.status = status;
            this.timestamp = timestamp;
        }

        // Getters and Setters
        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getDetails() {
            return details;
        }

        public void setDetails(String details) {
            this.details = details;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }
    }
}
