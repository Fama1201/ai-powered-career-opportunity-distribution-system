package com.jobifycvut.backend.dto;
import java.time.LocalDate;

public class OpportunityDetailResponse {
    private Long id;
    private String title;
    private String company;
    private String jobType;
    private String description;
    private String homeOffice;
    private String wage;
    private String benefits;
    private String formalRequirements;
    private String technicalRequirements;
    private String contactPerson;
    private String url;
    private LocalDate applicationDeadline;

    private boolean applied;
    private boolean saved;


    public OpportunityDetailResponse() {};
    public OpportunityDetailResponse(Long id, String title, String company, String jobType,
                                     String description, String homeOffice, String wage, String benefits,
                                     String formalRequirements, String technicalRequirements, String contactPerson,
                                     String url, LocalDate applicationDeadline,  boolean applied, boolean saved) {
        this.id = id;
        this.title = title;
        this.company = company;
        this.jobType = jobType;
        this.description = description;
        this.homeOffice = homeOffice;
        this.wage = wage;
        this.benefits = benefits;
        this.formalRequirements = formalRequirements;
        this.technicalRequirements = technicalRequirements;
        this.contactPerson = contactPerson;
        this.url = url;
        this.applicationDeadline = applicationDeadline;
        this.applied = applied;
        this.saved = saved;
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

    public String getCompany() {
        return company;
    }
    public void setCompany(String company) {
        this.company = company;
    }

    public String getJobType() {
        return jobType;
    }
    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getHomeOffice() {
        return homeOffice;
    }
    public void setHomeOffice(String homeOffice) {
        this.homeOffice = homeOffice;
    }

    public String getWage() {
        return wage;
    }
    public void setWage(String wage) {
        this.wage = wage;
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

    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }

    public LocalDate getApplicationDeadline() {
        return applicationDeadline;
    }
    public void setApplicationDeadline(LocalDate applicationDeadline) {
        this.applicationDeadline = applicationDeadline;
    }

    public boolean isApplied() {
        return applied;
    }
    public void setApplied(boolean applied) {
        this.applied = applied;
    }

    public boolean isSaved() {
        return saved;
    }
    public void setSaved(boolean saved) {
        this.saved = saved;
    }
}
