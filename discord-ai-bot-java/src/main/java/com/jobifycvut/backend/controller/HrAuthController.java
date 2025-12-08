package com.jobifycvut.backend.controller;

import com.jobifycvut.backend.dto.HrLoginRequest;
import com.jobifycvut.backend.dto.HrRegisterRequest;
import com.jobifycvut.backend.model.HrUser;
import com.jobifycvut.backend.service.HrService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller handling all HR authentication and authorization endpoints.
 * All paths under this controller are public (allowed by SecurityConfig).
 */
@RestController
@RequestMapping("/api/hr/auth") // Base path for all HR authentication endpoints
@RequiredArgsConstructor // Lombok creates the constructor for final fields (HrService)
public class HrAuthController {

    // Dependency injection of the business logic layer
    private final HrService hrService;

    /**
     * Endpoint for registering a new HR user account.
     * Delegates validation and persistence to the HrService.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody HrRegisterRequest request) {
        System.out.println("📝 HR Register attempt: " + request.getEmail());
        try {
            // 1. Delegate registration logic (hashing, validation, saving) to the Service
            hrService.register(request);

            // 2. Return success response
            return ResponseEntity.ok(Map.of("message", "HR User registered successfully"));

        } catch (RuntimeException e) {
            // Catch exceptions, such as 'Email is already registered'
            // thrown by the service layer, and return HTTP 400 Bad Request.
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Endpoint for user authentication (Login).
     * Delegates credential verification to the HrService.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody HrLoginRequest request) {
        System.out.println("🔥 HR Login attempt: " + request.getEmail());
        try {
            // 1. Delegate authentication (user lookup, password match) to the Service
            HrUser user = hrService.login(request);

            // 2. Return success response with session details
            return ResponseEntity.ok(Map.of(
                    "message", "Login successful",
                    // NOTE: This is currently a placeholder. To be replaced with a real JWT.
                    "token", "dummy-token-12345",
                    "email", user.getEmail(),
                    "company", user.getCompanyName() != null ? user.getCompanyName() : "N/A"
            ));
        } catch (RuntimeException e) {
            // Catch exceptions, such as 'User not found' or 'Invalid credentials'
            // and return HTTP 401 Unauthorized.
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}