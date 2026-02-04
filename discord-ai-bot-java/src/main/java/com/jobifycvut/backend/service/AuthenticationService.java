package com.jobifycvut.backend.service;

import com.jobifycvut.backend.dto.*;
import com.jobifycvut.backend.exception.AuthException;
import com.jobifycvut.backend.model.User;
import com.jobifycvut.backend.model.UserRole;
import com.jobifycvut.backend.repository.UserRepository;
import com.jobifycvut.backend.util.JwtUtil;
import com.jobifycvut.backend.util.PasswordHasher;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    private static final boolean EMAIL_VERIFICATION_ENABLED = false;

    public AuthenticationService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AuthException("Email already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(PasswordHasher.hashPassword(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setActive(true);
        user.setRole(UserRole.STUDENT);
        user.setCreatedAt(Instant.now());

        if (EMAIL_VERIFICATION_ENABLED) {
            user.setEmailVerifiedAt(null);
            user.setEmailVerificationToken(UUID.randomUUID().toString());
        } else {
            user.setEmailVerifiedAt(Instant.now());
            user.setEmailVerificationToken(null);
        }

        User savedUser = userRepository.save(user);

        AuthResponse authResponse = new AuthResponse();
        authResponse.setMessage("Registration successful. Please verify your email address.");
        authResponse.setUserId(savedUser.getId());
        authResponse.setRequiresVerification(true);

        return authResponse;
    }

    public AuthResponse login(LoginRequest request) {
        try {
            // Log for debugging
            System.out.println("Login attempt for email: " + request.getEmail());
            
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> {
                        System.out.println("User not found for email: " + request.getEmail());
                        return new AuthException("Invalid email or password.");
                    });

            System.out.println("User found: " + user.getEmail() + ", Role: " + user.getRole());
            
            if (!PasswordHasher.verifyPassword(request.getPassword(), user.getPassword())) {
                System.out.println("Password verification failed for user: " + user.getEmail());
                throw new AuthException("Invalid email or password.");
            }
            
            System.out.println("Password verification successful for user: " + user.getEmail());

            // Validate user role
            if (user.getRole() == null) {
                throw new AuthException("User role is not set. Please contact support.");
            }

            String accessToken = jwtUtil.generateAccessToken(user);
            String refreshToken = jwtUtil.generateRefreshToken(user);

            user.setLastLoginAt(Instant.now());
            userRepository.save(user);

            long expiresInSeconds = 15 * 60L;

            AuthResponse response = new AuthResponse();
            response.setAccessToken(accessToken);
            response.setRefreshToken(refreshToken);
            response.setTokenType("Bearer");
            response.setExpiresIn(String.valueOf(expiresInSeconds));
            response.setUserId(user.getId());
            response.setEmail(user.getEmail());
            response.setFirstName(user.getFirstName());
            response.setLastName(user.getLastName());
            response.setRole(user.getRole().toString());
            response.setMessage("Login Successful");

            return response;
        } catch (AuthException e) {
            // Re-throw AuthException as-is
            System.err.println("AuthException during login: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            // Log unexpected errors with full details
            System.err.println("=== UNEXPECTED ERROR DURING LOGIN ===");
            System.err.println("Exception Type: " + e.getClass().getName());
            System.err.println("Exception Message: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("Cause: " + e.getCause().getClass().getName() + " - " + e.getCause().getMessage());
            }
            e.printStackTrace();
            System.err.println("=====================================");
            
            // Return more specific error message
            String errorMsg = e.getMessage();
            if (errorMsg == null || errorMsg.isEmpty()) {
                errorMsg = "An error occurred during login. Please try again.";
            }
            throw new AuthException(errorMsg, e);
        }
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        Map<String, Object> claims = jwtUtil.validateToken(request.getRefreshToken());

        if (claims == null) {
            throw new AuthException("Invalid or expired refresh token.");
        }
        if (!"refresh".equals(String.valueOf(claims.get("type")))) {
            throw new AuthException("Invalid token type.");
        }

        Long userId = ((Number) claims.get("userId")).longValue();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException("User not found."));

        String newAccessToken = jwtUtil.generateAccessToken(user);

        long expiresInSeconds = 15 * 60L;

        AuthResponse response = new AuthResponse();
        response.setAccessToken(newAccessToken);
        response.setRefreshToken(request.getRefreshToken());
        response.setTokenType("Bearer");
        response.setExpiresIn(String.valueOf(expiresInSeconds));
        response.setMessage("Token refreshed.");

        return response;
    }

    public TokenValidationResponse verifyToken(String token) {
        Map<String, Object> claims = jwtUtil.validateToken(token);
        TokenValidationResponse response = new TokenValidationResponse();

        if (claims == null) {
            response.setValid(false);
            response.setError("Invalid or expired token.");
            return response;
        }

        response.setValid(true);

        long uid = ((Number) claims.get("userId")).longValue();
        response.setUserId(String.valueOf(uid));
        response.setEmail(String.valueOf(claims.get("sub")));
        response.setRole(String.valueOf(claims.get("role")));
        response.setExpiresAt(Instant.ofEpochSecond(((Number) claims.get("exp")).longValue()));

        return response;
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        var userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isEmpty()) return;

        User user = userOpt.get();
        String resetToken = UUID.randomUUID().toString();

        user.setPasswordResetToken(resetToken);
        user.setPasswordResetTokenExpiry(Instant.now().plus(1, ChronoUnit.HOURS));
        userRepository.save(user);

        System.out.println("Reset Token for: " + user.getEmail() + ": " + resetToken);
    }

    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByPasswordResetToken(request.getToken())
                .orElseThrow(() -> new AuthException("Invalid reset token."));

        if (user.getPasswordResetTokenExpiry().isBefore(Instant.now())) {
            throw new AuthException("Token has expired.");
        }

        user.setPassword(PasswordHasher.hashPassword(request.getNewPassword()));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        userRepository.save(user);
    }

    public void logout(String token) {
        System.out.println("User logged out.");
    }

    public MessageResponse verifyEmail(String token) {
        User user = userRepository.findByEmail(token)
                .orElseThrow(() -> new AuthException("Invalid email."));

        user.setActive(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerifiedAt(Instant.now());
        userRepository.save(user);

        return new MessageResponse("Email verified successfully.");
    }
}
