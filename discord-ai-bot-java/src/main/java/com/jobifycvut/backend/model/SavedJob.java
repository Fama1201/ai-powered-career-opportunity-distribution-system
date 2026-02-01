package com.jobifycvut.backend.model;

import java.time.Instant;
import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name="saved_jobs",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "opportunity_id"})
)
public class SavedJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opportunity_id", nullable = false)
    private Opportunity opportunity;

    @Column(name = "saved_at", nullable = false)
    private Instant savedAt;

    @PrePersist
    public void onSave() {
        if(savedAt == null) {
            savedAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Opportunity getOpportunity() {
        return opportunity;
    }
    public void setOpportunity(Opportunity opportunity) {
        this.opportunity = opportunity;
    }

    public Instant getSavedAt() {
        return savedAt;
    }
    public void setSavedAt(Instant savedAt) {
        this.savedAt = savedAt;
    }

}
