package com.jobifycvut.backend.dto;

public class RefreshTokenRequest {
    private String refreshToken;

    public RefreshTokenRequest() {};
    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    public String getRefreshToken() {
        return refreshToken;
    }
}
