package com.jobifycvut.backend.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="notifications")
public class NotificationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name="message", columnDefinition ="text", nullable = false)
    private String message;

    @Transient // type kolonu database'de yok, bu yüzden transient yapıyoruz
    private String type;

    @Column(name="is_read", nullable = false) // PostgreSQL'de "read" reserved keyword olduğu için is_read kullanıyoruz
    private Boolean read = false;

    @Column(name="created_at", nullable = false)
    private Instant createdAt;

    public NotificationEntity() {}

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
    
    // Backward compatibility - deprecated, use getUserId instead
    @Deprecated
    public Long getStudentId() {
        return userId;
    }
    @Deprecated
    public void setStudentId(Long studentId) {
        this.userId = studentId;
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

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
}
