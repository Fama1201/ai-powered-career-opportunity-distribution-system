package bot.storage;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "opportunities")
public class Opportunity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String title;
    private String description;

    @Column(name = "job_type")
    private String jobType;

    @Column(name = "application_deadline")
    private LocalDate applicationDeadline;

    private String url;
    private String wage;

    @Column(name = "home_office")
    private String homeOffice;

    private String benefits;

    @Column(name = "formal_requirements")
    private String formalRequirements;

    @Column(name = "technical_requirements")
    private String technicalRequirements;

    @Column(name = "contact_person")
    private String contactPerson;

    private String company;

    // --- NEW FIELDS FROM YOUR SCHEMA ---
    @Column(name = "discord_id")
    private String discordId;

    @Column(name = "opportunity_id")
    private String opportunityId;

    // --- Getters and Setters ---
    // (Spring needs these to read the data and send it as JSON)

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    public String getDiscordId() {
        return discordId;
    }

    public void setDiscordId(String discordId) {
        this.discordId = discordId;
    }

    public String getOpportunityId() {
        return opportunityId;
    }

    public void setOpportunityId(String opportunityId) {
        this.opportunityId = opportunityId;
    }
}