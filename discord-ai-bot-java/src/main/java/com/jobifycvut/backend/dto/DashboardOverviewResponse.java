package com.jobifycvut.backend.dto;

import java.util.List;

/**
 * DTO for the main dashboard overview.
 * Summarizes the student's current status in one request.
 */
public class DashboardOverviewResponse {
    private String studentName;
    private int totalApplications;
    private int savedJobsCount;
    private List<OpportunityListResponse> recentMatches;

    // Constructors
    public DashboardOverviewResponse() {}
    public DashboardOverviewResponse(String studentName, int totalApplications, int savedJobsCount, List<OpportunityListResponse> recentMatches) {
        this.studentName = studentName;
        this.totalApplications = totalApplications;
        this.savedJobsCount = savedJobsCount;
        this.recentMatches = recentMatches;
    }

    // Getters and Setters
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public int getTotalApplications() { return totalApplications; }
    public void setTotalApplications(int totalApplications) { this.totalApplications = totalApplications; }

    public int getSavedJobsCount() { return savedJobsCount; }
    public void setSavedJobsCount(int savedJobsCount) { this.savedJobsCount = savedJobsCount; }

    public List<OpportunityListResponse> getRecentMatches() { return recentMatches; }
    public void setRecentMatches(List<OpportunityListResponse> recentMatches) { this.recentMatches = recentMatches; }
}