package com.jobifycvut.backend.dto;

public class StudentProfileUpdateRequest {
    private String name;
    private String skills;
    private String careerInterest;

    public StudentProfileUpdateRequest() {}

    public StudentProfileUpdateRequest(String name, String skills, String careerInterest) {
        this.name = name;
        this.skills = skills;
        this.careerInterest = careerInterest;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getSkills() {
        return skills;
    }
    public void setSkills(String skills) {
        this.skills = skills;
    }
    public String getCareerInterest() {
        return careerInterest;
    }
    public void setCareerInterest(String careerInterest) {
        this.careerInterest = careerInterest;
    }
}
