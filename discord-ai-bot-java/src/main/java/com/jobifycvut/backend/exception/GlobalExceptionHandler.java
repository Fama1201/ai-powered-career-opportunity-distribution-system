package com.jobifycvut.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice(basePackages = "com.jobifycvut.backend.controller")
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<Map<String, Object>> handleAuthException(AuthException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "message", ex.getMessage(),
                        "error", "AUTH_ERROR"
                ));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "message", ex.getMessage(),
                        "error", "NOT_FOUND"
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        // Log the full exception for debugging
        System.err.println("=== EXCEPTION DETAILS ===");
        System.err.println("Exception Type: " + ex.getClass().getName());
        System.err.println("Exception Message: " + ex.getMessage());
        ex.printStackTrace();
        System.err.println("========================");
        
        // Return user-friendly error message
        String message = ex.getMessage();
        if (message == null || message.isEmpty()) {
            message = "An unexpected error occurred. Please try again later.";
        }
        
        // Include cause message if available
        if (ex.getCause() != null && ex.getCause().getMessage() != null) {
            message += " (" + ex.getCause().getMessage() + ")";
        }
        
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "message", message,
                        "error", "INTERNAL_SERVER_ERROR",
                        "exceptionType", ex.getClass().getSimpleName()
                ));
    }
}
