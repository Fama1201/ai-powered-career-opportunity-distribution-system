package com.jobifycvut.backend.dto;

import java.time.OffsetDateTime;

public class HrApplicationListItemResponse {
    private Long applicationId;
    private Long jobId;
    private Long studentUserId;
    private String studentName;
    private String studentEmail;
    private OffsetDateTime appliedAt;
    private String status;

    public HrApplicationListItemResponse() {}

    public HrApplicationListItemResponse(Long applicationId, Long jobId, Long studentUserId,
                                         String studentName, String studentEmail,
                                         OffsetDateTime appliedAt, String status) {
        this.applicationId = applicationId;
        this.jobId = jobId;
        this.studentUserId = studentUserId;
        this.studentName = studentName;
        this.studentEmail = studentEmail;
        this.appliedAt = appliedAt;
        this.status = status;
    }

    public Long getApplicationId() { return applicationId; }
    public Long getJobId() { return jobId; }
    public Long getStudentUserId() { return studentUserId; }
    public String getStudentName() { return studentName; }
    public String getStudentEmail() { return studentEmail; }
    public OffsetDateTime getAppliedAt() { return appliedAt; }
    public String getStatus() { return status; }
}
