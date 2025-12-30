package com.jobifycvut.backend.dto;

/**
 * Data Transfer Object (DTO) for the Forgot Password feature.
 * It captures the email address provided by the user to request a password reset link.
 */
public class ForgotPasswordRequest {

    // The email address where the reset link should be sent
    private String email;

    // --- Getters and Setters ---

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}