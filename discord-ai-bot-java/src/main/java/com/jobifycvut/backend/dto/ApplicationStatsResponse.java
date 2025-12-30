package com.jobifycvut.backend.dto;

public class ApplicationStatsResponse {
    private int totalApplied;
    private int interviews; // Placeholder for future feature
    private int rejected;   // Placeholder for future feature

    public ApplicationStatsResponse(int totalApplied) {
        this.totalApplied = totalApplied;
        this.interviews = 0; // Defaulting to 0 for now
        this.rejected = 0;   // Defaulting to 0 for now
    }

    public int getTotalApplied() { return totalApplied; }
    public void setTotalApplied(int totalApplied) { this.totalApplied = totalApplied; }

    public int getInterviews() { return interviews; }
    public void setInterviews(int interviews) { this.interviews = interviews; }

    public int getRejected() { return rejected; }
    public void setRejected(int rejected) { this.rejected = rejected; }
}