package com.jobifycvut.backend.dto;

public class HrJobStatusRequest {
    private String status; // OPEN, CLOSED, ARCHIVED

    public HrJobStatusRequest() {}

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
