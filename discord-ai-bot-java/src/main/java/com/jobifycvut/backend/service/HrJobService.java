package com.jobifycvut.backend.service;

import com.jobifycvut.backend.dto.HrJobCreateRequest;
import com.jobifycvut.backend.model.Opportunity;
import com.jobifycvut.backend.repository.OpportunityRepository;
import com.jobifycvut.backend.security.SecurityUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class HrJobService {

    private final OpportunityRepository opportunityRepository;

    public HrJobService(OpportunityRepository opportunityRepository) {
        this.opportunityRepository = opportunityRepository;
    }

    // POST /api/hr/jobs
    public Opportunity createJob(HrJobCreateRequest req) {
        Long hrId = SecurityUtil.requireUserId();
        String ownerId = String.valueOf(hrId); // ✅ store as String because discord_id is varchar

        Opportunity o = new Opportunity();

        // Ownership (no DB changes): use discord_id as HR owner id (string)
        o.setDiscordId(ownerId);

        o.setTitle(req.getTitle());
        o.setDescription(req.getDescription());
        o.setJobType(req.getJobType());

        // Convert LocalDateTime -> LocalDate if needed
        o.setApplicationDeadline(toLocalDate(req.getApplicationDeadline()));

        o.setUrl(req.getUrl());
        o.setWage(req.getWage());
        o.setHomeOffice(req.getHomeOffice());
        o.setBenefits(req.getBenefits());
        o.setFormalRequirements(req.getFormalRequirements());
        o.setTechnicalRequirements(req.getTechnicalRequirements());
        o.setContactPerson(req.getContactPerson());
        o.setCompany(req.getCompany());

        if (o.getOpportunityId() == null || o.getOpportunityId().isBlank()) {
            o.setOpportunityId("HR-" + UUID.randomUUID());
        }

        return opportunityRepository.save(o);
    }

    // GET /api/hr/jobs?status=OPEN|CLOSED|ARCHIVED
    public List<Opportunity> listMyJobs(String status) {
        Long hrId = SecurityUtil.requireUserId();
        String ownerId = String.valueOf(hrId);
        LocalDate today = LocalDate.now();

        if (status == null || status.isBlank()) {
            return opportunityRepository.findByDiscordIdOrderByIdDesc(ownerId);
        }

        String s = status.trim().toUpperCase();

        return switch (s) {
            case "OPEN" ->
                    opportunityRepository.findByDiscordIdAndApplicationDeadlineGreaterThanEqualOrderByIdDesc(ownerId, today);
            case "CLOSED" ->
                    opportunityRepository.findByDiscordIdAndApplicationDeadlineLessThanOrderByIdDesc(ownerId, today);
            case "ARCHIVED" ->
                    opportunityRepository.findByDiscordIdAndApplicationDeadlineIsNullOrderByIdDesc(ownerId);
            default ->
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status (OPEN, CLOSED, ARCHIVED)");
        };
    }

    // GET /api/hr/jobs/{jobId}
    public Opportunity getMyJob(Long jobId) {
        return requireOwned(jobId);
    }

    // PUT /api/hr/jobs/{jobId}
    @Transactional
    public Opportunity updateMyJob(Long jobId, HrJobCreateRequest req) {
        Opportunity job = requireOwned(jobId);

        if (req.getTitle() != null) job.setTitle(req.getTitle());
        if (req.getDescription() != null) job.setDescription(req.getDescription());
        if (req.getJobType() != null) job.setJobType(req.getJobType());

        if (req.getApplicationDeadline() != null) {
            job.setApplicationDeadline(toLocalDate(req.getApplicationDeadline()));
        }

        if (req.getUrl() != null) job.setUrl(req.getUrl());
        if (req.getWage() != null) job.setWage(req.getWage());
        if (req.getHomeOffice() != null) job.setHomeOffice(req.getHomeOffice());
        if (req.getBenefits() != null) job.setBenefits(req.getBenefits());
        if (req.getFormalRequirements() != null) job.setFormalRequirements(req.getFormalRequirements());
        if (req.getTechnicalRequirements() != null) job.setTechnicalRequirements(req.getTechnicalRequirements());
        if (req.getContactPerson() != null) job.setContactPerson(req.getContactPerson());
        if (req.getCompany() != null) job.setCompany(req.getCompany());

        return job;
    }

    // PATCH /api/hr/jobs/{jobId}/status
    @Transactional
    public void updateStatus(Long jobId, String status) {
        Opportunity job = requireOwned(jobId);

        String s = status == null ? "" : status.trim().toUpperCase();
        LocalDate today = LocalDate.now();

        switch (s) {
            case "OPEN" -> {
                if (job.getApplicationDeadline() == null || job.getApplicationDeadline().isBefore(today)) {
                    job.setApplicationDeadline(today.plusDays(30));
                }
            }
            case "CLOSED" -> job.setApplicationDeadline(today.minusDays(1));
            case "ARCHIVED" -> job.setApplicationDeadline(null);
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status (OPEN, CLOSED, ARCHIVED)");
        }
    }

    // DELETE /api/hr/jobs/{jobId}
    public void deleteJob(Long jobId) {
        Opportunity job = requireOwned(jobId);
        opportunityRepository.delete(job);
    }

    private Opportunity requireOwned(Long jobId) {
        Long hrId = SecurityUtil.requireUserId();
        String ownerId = String.valueOf(hrId);

        Opportunity job = opportunityRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));

        // ✅ compare String-to-String
        if (job.getDiscordId() == null || !job.getDiscordId().equals(ownerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed");
        }

        return job;
    }

    // Converts LocalDate / LocalDateTime safely into LocalDate
    private LocalDate toLocalDate(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDate d) return d;
        if (value instanceof LocalDateTime dt) return dt.toLocalDate();
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid applicationDeadline type");
    }
}
