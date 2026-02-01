package com.jobifycvut.backend.dto;

public class JobActionRequest {
    private Long userId;

    public JobActionRequest() {};

    public JobActionRequest(Long userId) {
        this.userId = userId;
    }
    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
