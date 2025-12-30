package com.jobifycvut.backend.dto;

/**
 * Data Transfer Object (DTO) used to capture user input when an HR recruiter
 * attempts to register a new account via the API.
 * * NOTE: This DTO contains the raw password, which should be hashed immediately by the Service layer.
 */
public class HrRegisterRequest {

    // User credentials
    private String email;
    private String password; // Raw password input from the user

    // User profile information
    private String fullName;    // Mapped directly to 'full_name' column in the database (DB).
    private String companyName; // CRITICAL FIELD: Added to match the 'company_name' column in the DB table.


    // --- Getters and Setters ---

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
}