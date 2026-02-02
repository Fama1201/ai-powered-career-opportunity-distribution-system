package com.jobifycvut.backend.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="notifications")
public class NotificationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id")
    private Long studentId;

    @Column(name="company_id")
    private Long companyId;

    @Column(name="message", columnDefinition ="text", nullable = false)
    private String message;

    @Column(name="is_read", nullable = false)
    private Boolean read=false;

    @Column(name="created_at", nullable = false)
    private Instant createdAt;

    public NotificationEntity() {}

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
    public Long getCompanyId() {
        return companyId;
    }
    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public Boolean getRead() {
        return read;
    }
    public void setRead(Boolean read) {
        this.read = read;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }



}
