package com.jobifycvut.backend.dto;

import java.time.OffsetDateTime;

public class HrApplicationDetailResponse {
    private Long applicationId;
    private Long jobId;
    private Long studentUserId;

    private String studentName;
    private String studentEmail;
    private String studentSkills;
    private String studentCareerInterest;
    private String studentCvText;

    private OffsetDateTime appliedAt;
    private String status;
    private String notes;

    public HrApplicationDetailResponse() {}

    public HrApplicationDetailResponse(Long applicationId, Long jobId, Long studentUserId,
                                       String studentName, String studentEmail,
                                       String studentSkills, String studentCareerInterest, String studentCvText,
                                       OffsetDateTime appliedAt, String status, String notes) {
        this.applicationId = applicationId;
        this.jobId = jobId;
        this.studentUserId = studentUserId;
        this.studentName = studentName;
        this.studentEmail = studentEmail;
        this.studentSkills = studentSkills;
        this.studentCareerInterest = studentCareerInterest;
        this.studentCvText = studentCvText;
        this.appliedAt = appliedAt;
        this.status = status;
        this.notes = notes;
    }

    public Long getApplicationId() { return applicationId; }
    public Long getJobId() { return jobId; }
    public Long getStudentUserId() { return studentUserId; }
    public String getStudentName() { return studentName; }
    public String getStudentEmail() { return studentEmail; }
    public String getStudentSkills() { return studentSkills; }
    public String getStudentCareerInterest() { return studentCareerInterest; }
    public String getStudentCvText() { return studentCvText; }
    public OffsetDateTime getAppliedAt() { return appliedAt; }
    public String getStatus() { return status; }
    public String getNotes() { return notes; }
}
