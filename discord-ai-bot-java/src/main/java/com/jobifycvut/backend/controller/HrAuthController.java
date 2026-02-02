package com.jobifycvut.backend.controller;

import com.jobifycvut.backend.dto.ForgotPasswordRequest;
import com.jobifycvut.backend.dto.HrLoginRequest;
import com.jobifycvut.backend.dto.HrRegisterRequest;
import com.jobifycvut.backend.model.HrUser;
import com.jobifycvut.backend.service.HrService;
import com.jobifycvut.backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/hr/auth")
@RequiredArgsConstructor
public class HrAuthController {

    private final HrService hrService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody HrRegisterRequest request) {
        try {
            hrService.register(request);
            return ResponseEntity.ok(Map.of("message", "HR User registered successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody HrLoginRequest request) {
        try {
            // 1. Authenticate
            HrUser user = hrService.login(request);

            // 2. Generate Tokens using the new JwtUtil method
            String accessToken = jwtUtil.generateAccessToken(user);
            String refreshToken = jwtUtil.generateRefreshToken(user);

            // 3. Return Response
            return ResponseEntity.ok(Map.of(
                    "message", "Login successful",
                    "accessToken", accessToken,
                    "refreshToken", refreshToken,
                    "email", user.getEmail(),
                    "company", user.getCompanyName() != null ? user.getCompanyName() : "N/A"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody(required = false) Map<String, String> body) {
        String email = (body != null) ? body.get("email") : null;
        hrService.logout(email);
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            hrService.forgotPassword(request);
            // We return 200 even if user is not found to prevent email enumeration (security best practice),
            // but for this project, the service throws "User not found", so we catch it.
            return ResponseEntity.ok(Map.of("message", "If an account exists, a reset email has been sent."));
        } catch (RuntimeException e) {
            // In development, you might want to see the error.
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }
}