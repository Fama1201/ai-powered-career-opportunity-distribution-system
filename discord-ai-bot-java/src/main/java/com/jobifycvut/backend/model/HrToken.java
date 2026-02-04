package com.jobifycvut.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Represents the 'hr_tokens' table in the PostgreSQL database.
 * Stores tokens for HR users (e.g., API tokens, session tokens).
 */
@Entity
@Table(name = "hr_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HrToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hr_id", nullable = false)
    private HrUser hr;

    @Column(name = "token", nullable = false, unique = true)
    private String token;
}

