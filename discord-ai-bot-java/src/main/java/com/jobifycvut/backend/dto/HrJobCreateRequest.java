package com.jobifycvut.backend.dto;

import java.time.LocalDateTime;
public class HrJobCreateRequest {

    private String title;
    private String description;
    private String jobType;
    private LocalDateTime applicationDeadline;
    private String url;
    private String wage;
    private String homeOffice;
    private String benefits;
    private String formalRequirements;
    private String technicalRequirements;
    private String contactPerson;
    private String company;

    public HrJobCreateRequest() {}
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
    public LocalDateTime getApplicationDeadline() {
        return applicationDeadline;
    }
    public void setApplicationDeadline(LocalDateTime applicationDeadline) {
        this.applicationDeadline = applicationDeadline;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
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

}
