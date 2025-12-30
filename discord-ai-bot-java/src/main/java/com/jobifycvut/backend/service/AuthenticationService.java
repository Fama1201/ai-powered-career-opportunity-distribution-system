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
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private static final boolean EMAIL_VERIFICATION_ENABLED = false;


    public AuthenticationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public AuthResponse register(RegisterRequest request){
        if(userRepository.existsByEmail(request.getEmail())){
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
        user.setEmailVerificationToken(UUID.randomUUID().toString());

        if (EMAIL_VERIFICATION_ENABLED) {
            // real email flow will go here
            user.setEmailVerifiedAt(null);
            user.setEmailVerificationToken(UUID.randomUUID().toString());
        } else {
            // 🔥 Dev Mode: User verified instantly
            user.setEmailVerifiedAt(Instant.now());
            user.setEmailVerificationToken(null);
        }

        User savedUser = userRepository.save(user);

        System.out.println("📧 Email verification token: " + savedUser.getEmailVerificationToken());

        AuthResponse authResponse = new AuthResponse();
        authResponse.setMessage("Registration successful. Please verify your email address.");
        authResponse.setUserId(savedUser.getId());
        authResponse.setRequiresVerification(true);

        return authResponse;
    }

    public AuthResponse login(LoginRequest request){
        User user= userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthException("Invalid email or password."));

        if(!PasswordHasher.verifyPassword(request.getPassword(), user.getPassword())){
            throw new AuthException("Invalid password.");
        }
        String accessToken=JwtUtil.generateAccessToken(user);
        String refreshToken=JwtUtil.generateRefreshToken(user);


        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        long expiresInSeconds=15*60L;

        AuthResponse response=new AuthResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setTokenType("Bearer");
        response.setExpiresIn(String.valueOf(expiresInSeconds));
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().toString());
        response.setMessage("Login Successful");

        return response;
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        Map<String, Object> claims = JwtUtil.validateToken(request.getRefreshToken());

        if (claims == null) {
            throw new AuthException("Invalid or expired refresh token.");
        }
        if (!"refresh".equals(claims.get("type"))) {
            throw new AuthException("Invalid token type.");
        }
        Long userId = ((Number) claims.get("userId")).longValue();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException("User not found."));

        String newAccessToken = JwtUtil.generateAccessToken(user);

        long expiresInSeconds = 15 * 60L;


        AuthResponse response = new AuthResponse();
        response.setAccessToken(newAccessToken);
        response.setRefreshToken(request.getRefreshToken());
        response.setTokenType("Bearer");
        response.setExpiresIn(String.valueOf(expiresInSeconds));
        response.setMessage("Token refreshed.");
        return response;
    }

    public TokenValidationResponse verifyToken(String token){
        Map<String, Object> claims = JwtUtil.validateToken(token);
        TokenValidationResponse response = new TokenValidationResponse();

        if (claims == null) {
            response.setValid(false);
            response.setError("Invalid or expired token.");
            return response;
        }
        response.setValid(true);

        long uid = ((Number) claims.get("userId")).longValue();

        response.setUserId(String.valueOf(uid));
        response.setEmail((String) claims.get("sub"));
        response.setRole((String) claims.get("role"));
        response.setExpiresAt(Instant.ofEpochSecond(((Number) claims.get("exp")).longValue()));

        return response;
    }

    public void forgotPassword(ForgotPasswordRequest request){
        var userOpt = userRepository.findByEmail(request.getEmail());

        if(userOpt.isEmpty()){
            return;
        }
        User user=userOpt.get();
        String resetToken=UUID.randomUUID().toString();

        user.setPasswordResetToken(resetToken);
        user.setPasswordResetTokenExpiry(Instant.now().plus(1, ChronoUnit.HOURS));
        userRepository.save(user);

        System.out.println("Reset Token for:  " + user.getEmail() + ": " + resetToken);
    }

    public void resetPassword(ResetPasswordRequest request){
        User user=userRepository.findByPasswordResetToken(request.getToken())
                .orElseThrow(() -> new AuthException("Invalid reset token."));

        if(user.getPasswordResetTokenExpiry().isBefore(Instant.now())){
            throw new AuthException("Token has expired.");
        }

        user.setPassword(PasswordHasher.hashPassword(request.getNewPassword()));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        userRepository.save(user);
    }

    public void logout(String token){
        System.out.println("User logged out.");
    }

    public MessageResponse verifyEmail(String token){
        User user=userRepository.findByEmail(token)
                .orElseThrow(() -> new AuthException("Invalid email."));

        user.setActive(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerifiedAt(Instant.now());
        userRepository.save(user);

        return new MessageResponse("Email verified successfully.");
    }




}

