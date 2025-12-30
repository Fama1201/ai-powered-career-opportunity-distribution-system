package com.jobifycvut.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<Map<String, Object>> handleAuthException(AuthException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)  // 403, or 401 if you prefer
                .body(Map.of(
                        "message", ex.getMessage(),
                        "requiresVerification", true
                ));
    }
}
