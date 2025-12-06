package com.jobifycvut.backend.controller;

import com.jobifycvut.backend.dto.HrLoginRequest;
import com.jobifycvut.backend.dto.ForgotPasswordRequest;
import com.jobifycvut.backend.repository.HrUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for HR Authentication endpoints.
 * Handles Login, Logout, and Password Reset requests using the PostgreSQL database.
 * Base URL: /api/hr/auth
 */
@RestController
@RequestMapping("/api/hr/auth")
public class HrAuthController {

    // Inject the Repository so we can talk to the 'hr' table
    @Autowired
    private HrUserRepository hrUserRepository;

    /**
     * Endpoint: POST /api/hr/auth/login
     * Authenticates an HR user by checking the database.
     * * @param request The JSON body containing email and password.
     * @return 200 OK with token if successful, or 401 Unauthorized if failed.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody HrLoginRequest request) {
        System.out.println("🔥 HR Login attempt: " + request.getEmail());

        // 1. Check credentials against the database using the Repository
        boolean isValid = hrUserRepository.validateCredentials(request.getEmail(), request.getPassword());

        if (isValid) {
            // 2. Success: Return a success message and a token
            // TODO: In a production app, generate a real JWT here using a library like jjwt
            return ResponseEntity.ok(Map.of(
                    "message", "Login successful",
                    "token", "real-db-token-123",
                    "user", request.getEmail()
            ));
        } else {
            // 3. Failure: Return 401 Unauthorized
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid email or password"));
        }
    }

    /**
     * Endpoint: POST /api/hr/auth/logout
     * Logs out the current user.
     * * In a stateless JWT architecture, the server doesn't delete the session.
     * The frontend simply deletes the stored token.
     * * @return 200 OK
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        System.out.println("👋 HR Logout called");
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    /**
     * Endpoint: POST /api/hr/auth/forgot-password
     * Initiates the password reset process.
     * * @param request The JSON body containing the email address.
     * @return 200 OK (Generic message for security)
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        System.out.println("📧 Reset password requested for: " + request.getEmail());

        // TODO: Add logic here to check if email exists in DB and send an email via SMTP.
        // For now, we return a success message regardless to prevent user enumeration.

        return ResponseEntity.ok(Map.of("message", "If an account exists, a reset email has been sent."));
    }
}