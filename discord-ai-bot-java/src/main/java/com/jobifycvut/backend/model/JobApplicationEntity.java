package com.jobifycvut.backend.model;
import jakarta.persistence.*;
import java.time.OffsetDateTime;


@Entity
@Table(name="job_applications")
public class JobApplicationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name="user_id", nullable = false)
    private long userId;

    @Column(name="opportunity_id", nullable = false)
    private long opportunityId;

    @Column(name="applied_at", nullable = false)
    private OffsetDateTime appliedAt;

    @Column(name="status")
    private String status;

    @Column(name="notes")
    private String notes;

    public JobApplicationEntity() {}
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public long getUserId() {
        return userId;
    }
    public void setUserId(long userId) {
        this.userId = userId;
    }
    public long getOpportunityId() {
        return opportunityId;
    }
    public void setOpportunityId(long opportunityId) {
        this.opportunityId = opportunityId;
    }
    public OffsetDateTime getAppliedAt() {
        return appliedAt;
    }
    public void setAppliedAt(OffsetDateTime appliedAt) {
        this.appliedAt = appliedAt;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getNotes() {
        return notes;
    }
    public void setNotes(String notes) {
        this.notes = notes;
    }


}
