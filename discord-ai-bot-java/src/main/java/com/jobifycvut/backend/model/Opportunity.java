package com.jobifycvut.backend.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(
        name = "opportunities",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "unique_opportunity_per_user",
                        columnNames = {"opportunity_id", "discord_id"}
                )
        }
)
public class Opportunity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name= "job_type")
    private String jobType;

    @Column(name = "application_deadline")
    private LocalDate applicationDeadline;

    @Column(name= "discord_id")
    private String discordId;

    @Column(columnDefinition = "TEXT")
    private String url;

    @Column(name= "opportunity_id")
    private String opportunityId;

    private String wage;

    @Column(name="home_office")
    private String homeOffice;

    private String benefits;

    @Column(name="formal_requirements")
    private String formalRequirements;

    @Column(name= "technical_requirements")
    private String technicalRequirements;

    @Column(name= "contact_person")
    private String contactPerson;

    @Column(name = "company")
    private String company;

    // Note: Database schema uses 'company' as TEXT, not 'company_id' as FK
    // If you need to link to Company table, you'll need to add company_id column to database first

    // Note: The following fields are NOT in the database schema, so they are marked as @Transient
    // If you need them, add the columns to the database first
    @Transient
    private String requirements;

    @Transient
    private String location;

    @Transient
    private java.time.Instant createdAt;

    public  Opportunity() {
    }
    public Opportunity(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getJobType() {
        return jobType;
    }
    public void setJobType(String jobType) {
        this.jobType = jobType;
    }
    public LocalDate getApplicationDeadline() {
        return applicationDeadline;
    }
    public void setApplicationDeadline(LocalDate applicationDeadline) {
        this.applicationDeadline = applicationDeadline;
    }
    public String getDiscordId() {
        return discordId;
    }
    public void setDiscordId(String discordId) {
        this.discordId = discordId;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public String getOpportunityId() {
        return opportunityId;
    }
    public void setOpportunityId(String opportunityId) {
        this.opportunityId = opportunityId;
    }
    public String getWage() {
        return wage;
    }
    public void setWage(String wage) {
        this.wage = wage;
    }
    public String getHomeOffice() {
        return homeOffice;
    }
    public void setHomeOffice(String homeOffice) {
        this.homeOffice = homeOffice;
    }
    public String getBenefits() {
        return benefits;
    }
    public void setBenefits(String benefits) {
        this.benefits = benefits;
    }
    public String getFormalRequirements() {
        return formalRequirements;
    }
    public void setFormalRequirements(String formalRequirements) {
        this.formalRequirements = formalRequirements;
    }
    public String getTechnicalRequirements() {
        return technicalRequirements;
    }
    public void setTechnicalRequirements(String technicalRequirements) {
        this.technicalRequirements = technicalRequirements;
    }
    public String getContactPerson() {
        return contactPerson;
    }
    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }
    public String getCompany() {
        return company;
    }
    public void setCompany(String company) {
        this.company = company;
    }

    public String getRequirements() {
        return requirements;
    }
    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }

    public java.time.Instant getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(java.time.Instant createdAt) {
        this.createdAt = createdAt;
    }
}
