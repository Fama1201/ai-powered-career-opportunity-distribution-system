package com.jobifycvut.backend.dto;

/**
 * Calculates how complete the student profile is (0-100%).
 */
public class ProgressResponse {
    private int completionPercentage;
    private String message;

    public ProgressResponse(int completionPercentage, String message) {
        this.completionPercentage = completionPercentage;
        this.message = message;
    }

    public int getCompletionPercentage() { return completionPercentage; }
    public void setCompletionPercentage(int completionPercentage) { this.completionPercentage = completionPercentage; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}