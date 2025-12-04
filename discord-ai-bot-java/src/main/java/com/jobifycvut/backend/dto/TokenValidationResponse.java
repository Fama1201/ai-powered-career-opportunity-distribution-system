package com.jobifycvut.backend.dto;

public class TokenValidationResponse {
    private boolean valid;
    private String userId;
    private String email;
    private String role;
    private java.time.Instant expiresAt;
    private String error;

    public TokenValidationResponse() {};

    public boolean isValid() {
        return valid;
    }
    public void setValid(boolean valid) {
        this.valid = valid;
    }
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
    public java.time.Instant getExpiresAt() {
        return expiresAt;
    }
    public void setExpiresAt(java.time.Instant expiresAt) {
        this.expiresAt = expiresAt;
    }
    public String getError() {
        return error;
    }
    public void setError(String error) {
        this.error = error;
    }
}
