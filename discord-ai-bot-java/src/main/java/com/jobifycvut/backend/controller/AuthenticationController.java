package com.jobifycvut.backend.controller;

import com.jobifycvut.backend.dto.*;
import com.jobifycvut.backend.service.AuthenticationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request){
        AuthResponse authResponse = authenticationService.register(request);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request){
        AuthResponse authResponse = authenticationService.login(request);
        return ResponseEntity.ok(authResponse);
    }
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshTokenRequest request){
        AuthResponse authResponse=authenticationService.refreshToken(request);
        return ResponseEntity.ok(authResponse);
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@RequestBody ForgotPasswordRequest request){
        authenticationService.forgotPassword(request);
        MessageResponse response = new MessageResponse();
        return ResponseEntity.ok(response);
    }
    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@RequestBody ResetPasswordRequest request){
        authenticationService.resetPassword(request);

        MessageResponse response = new MessageResponse();
        return ResponseEntity.ok(response);
    }
    @GetMapping("/verify/{token}")
    public ResponseEntity<MessageResponse> verifyEmail(@PathVariable String token){
        MessageResponse response=authenticationService.verifyEmail(token);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/validate-token")
    public ResponseEntity<TokenValidationResponse> validateToken(@RequestBody String token) {
        TokenValidationResponse response = authenticationService.verifyToken(token);
        return ResponseEntity.ok(response);
    }

}
