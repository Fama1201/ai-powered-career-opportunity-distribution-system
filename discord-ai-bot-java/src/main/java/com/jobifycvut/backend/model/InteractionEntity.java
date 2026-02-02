package com.jobifycvut.backend.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name="interactions")
public class InteractionEntity {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name="opportunity_id")
    private Long opportunityId;

    @Column(name="action", nullable = false)
    private String action;

    @Column(name = "prompt", columnDefinition = "text")
    private String prompt;

    @Column(name="response", columnDefinition = "text")
    private String response;

    @Column(name="created_at", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    public void OnCreate(){
        if(createdAt==null){
            createdAt = OffsetDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getStudentId() {
        return studentId;
    }
    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }
    public Long getOpportunityId() {
        return opportunityId;
    }
    public void setOpportunityId(Long opportunityId) {
        this.opportunityId = opportunityId;
    }
    public String getAction() {
        return action;
    }
    public void setAction(String action) {
        this.action = action;
    }
    public String getPrompt() {
        return prompt;
    }
    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
    public String getResponse() {
        return response;
    }
    public void setResponse(String response) {
        this.response = response;
    }
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
