package com.jobifycvut.backend.dto;

import com.jobifycvut.backend.model.Opportunity;
import java.time.LocalDate;



public class OpportunityListResponse {
    private Long id;
    private String title;
    private String company;
    private String jobType;
    private String homeOffice;
    private String wage;
    private LocalDate applicationDeadline;
    private Integer matchScore; // Match score (0-100) calculated by MatchService

    public OpportunityListResponse(){};

    public OpportunityListResponse(Long id, String title, String company, String jobType, String homeOffice, String wage, LocalDate applicationDeadline){
        this.id=id;
        this.title=title;
        this.company=company;
        this.jobType=jobType;
        this.homeOffice=homeOffice;
        this.wage=wage;
        this.applicationDeadline=applicationDeadline;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id=id;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title=title;
    }

    public String getCompany() {
        return company;
    }
    public void setCompany(String company) {
        this.company=company;
    }

    public String getJobType() {
        return jobType;
    }
    public void setJobType(String jobType) {
        this.jobType=jobType;
    }

    public String getHomeOffice() {
        return homeOffice;
    }
    public void setHomeOffice(String homeOffice) {
        this.homeOffice=homeOffice;
    }

    public String getWage() {
        return wage;
    }
    public void setWage(String wage) {
        this.wage=wage;
    }

    public LocalDate getApplicationDeadline() {
        return applicationDeadline;
    }
    public void setApplicationDeadline(LocalDate applicationDeadline) {
        this.applicationDeadline=applicationDeadline;
    }

    public Integer getMatchScore() {
        return matchScore;
    }
    public void setMatchScore(Integer matchScore) {
        this.matchScore = matchScore;
    }
}
