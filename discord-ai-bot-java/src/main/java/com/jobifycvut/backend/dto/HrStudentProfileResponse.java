package com.jobifycvut.backend.dto;

public class HrStudentProfileResponse {
    private Long id;
    private String name;
    private String email;
    private String skills;
    private String careerInterest;

    public HrStudentProfileResponse(Long id, String name, String email, String skills, String careerInterest) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.skills = skills;
        this.careerInterest = careerInterest;
    }
    public Long getId() {
        return id;
    }
    public String  getName() {
        return name;
    }
    public String getEmail() {
        return email;
    }
    public String getSkills() {
        return skills;
    }
    public String getCareerInterest() {
        return careerInterest;
    }
}
