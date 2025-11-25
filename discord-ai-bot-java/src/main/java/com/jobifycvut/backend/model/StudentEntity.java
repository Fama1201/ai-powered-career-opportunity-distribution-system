package com.jobifycvut.backend.model;

import jakarta.persistence.*;

/**
 * Represents the 'student' table in the PostgreSQL database.
 * Each instance of this class corresponds to one row in the table.
 * This is the "Model" in the MVC architecture.
 */
@Entity
@Table(name = "student") // Maps this class to the existing 'student' table in the DB
public class StudentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increment ID (SERIAL in Postgres)
    private Long id;

    private String name;
    private String email;
    private String skills;

    // Maps the 'career_interest' column (snake_case) to 'careerInterest' variable (camelCase)
    @Column(name = "career_interest")
    private String careerInterest;

    // Stores the Discord ID. Must be unique to identify the user.
    @Column(name = "discord_id", unique = true)
    private String discordId;

    // Defines this column as TEXT to store large strings (like a full Resume/CV)
    @Column(name = "cv_text", columnDefinition = "TEXT")
    private String cvText;

    // --- Getters and Setters (Required for Spring JPA to handle data) ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }

    public String getCareerInterest() { return careerInterest; }
    public void setCareerInterest(String careerInterest) { this.careerInterest = careerInterest; }

    public String getDiscordId() { return discordId; }
    public void setDiscordId(String discordId) { this.discordId = discordId; }

    public String getCvText() { return cvText; }
    public void setCvText(String cvText) { this.cvText = cvText; }
}