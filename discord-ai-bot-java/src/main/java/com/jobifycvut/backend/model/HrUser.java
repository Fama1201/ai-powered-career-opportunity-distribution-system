package com.jobifycvut.backend.model;

import jakarta.persistence.*; // Standard JPA annotations (using Jakarta for Spring Boot 3+)
import lombok.Data;          // Generates getters, setters, toString(), equals(), and hashCode()
import lombok.NoArgsConstructor; // Generates a constructor with no arguments
import lombok.AllArgsConstructor; // Generates a constructor with all arguments
import java.time.LocalDateTime;

/**
 * Represents the 'hr' table in the PostgreSQL database.
 * This is the Entity used for persistence and data modeling for HR recruiters.
 */
@Entity
@Table(name = "hr") // Maps this Java class to the 'hr' table in the database
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HrUser {

    /**
     * Primary Key (PK) field.
     * Maps to the 'id' column, which is typically a serial/auto-incrementing integer in Postgres.
     * NOTE: We use GenerationType.IDENTITY for database-managed auto-incrementing IDs.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // If Postgres 'id' is defined as SERIAL (Integer), this should be Integer.

    /**
     * User's email address.
     * Constraint: Must be unique and non-null in the database.
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * Stores the securely hashed version of the user's password (e.g., BCrypt hash).
     * Maps to the 'password_hash' column.
     */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /**
     * The full name of the user.
     * Mapped explicitly using 'full_name' to follow PostgreSQL snake_case conventions.
     */
    @Column(name = "full_name")
    private String fullName;

    /**
     * The name of the company the recruiter works for.
     * Mapped explicitly using 'company_name'.
     */
    @Column(name = "company_name")
    private String companyName;

    /**
     * Timestamp indicating when the user account was created.
     * Mapped explicitly using 'created_at'.
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Field reserved for storing internal HR-related tokens (e.g., verification code, referral ID).
     * Mapped explicitly using 'hr_tokens'.
     */
    @Column(name = "hr_tokens")
    private String hrTokens;
}