package com.jobifycvut.backend.dto;

/**
 * Data Transfer Object (DTO) for HR Login.
 * This class maps the JSON body sent by the frontend (email, password)
 * into a Java object that the Controller can use.
 */
public class HrLoginRequest {

    // The email address of the HR user
    private String email;

    // The plain text password sent from the frontend
    private String password;

    // --- Getters and Setters ---

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}