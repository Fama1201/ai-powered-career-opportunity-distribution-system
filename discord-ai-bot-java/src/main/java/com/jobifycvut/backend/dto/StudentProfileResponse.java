package com.jobifycvut.backend.dto;

public class StudentProfileResponse {
    private long id;
    private String name;
    private String email;
    private String skills;
    private String careerInterest;

    public StudentProfileResponse(){}

    public StudentProfileResponse(long id, String name, String email, String skills, String careerInterest) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.skills = skills;
        this.careerInterest = careerInterest;
    }

    public Long getId(){
        return id;
    }
    public void setId(long id){
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
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
