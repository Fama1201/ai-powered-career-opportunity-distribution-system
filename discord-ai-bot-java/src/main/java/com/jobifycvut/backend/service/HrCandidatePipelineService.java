package com.jobifycvut.backend.service;

import com.jobifycvut.backend.dto.HrApplicationDetailResponse;
import com.jobifycvut.backend.dto.HrApplicationListItemResponse;
import com.jobifycvut.backend.model.JobApplicationEntity;
import com.jobifycvut.backend.model.Opportunity;
import com.jobifycvut.backend.model.StudentEntity;
import com.jobifycvut.backend.model.User;
import com.jobifycvut.backend.repository.JobApplicationRepository;
import com.jobifycvut.backend.repository.OpportunityRepository;
import com.jobifycvut.backend.repository.StudentRepository;
import com.jobifycvut.backend.repository.UserRepository;
import com.jobifycvut.backend.security.SecurityUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class HrCandidatePipelineService {

    private final JobApplicationRepository applicationRepository;
    private final OpportunityRepository opportunityRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    public HrCandidatePipelineService(JobApplicationRepository applicationRepository,
                                      OpportunityRepository opportunityRepository,
                                      UserRepository userRepository,
                                      StudentRepository studentRepository) {
        this.applicationRepository = applicationRepository;
        this.opportunityRepository = opportunityRepository;
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
    }

    // GET /api/hr/jobs/{jobId}/applications
    public List<HrApplicationListItemResponse> listApplicantsForJob(Long jobId) {
        Opportunity job = requireOwnedJob(jobId);

        List<JobApplicationEntity> apps = applicationRepository.findByOpportunityId(job.getId());

        return apps.stream().map(app -> {
            User u = userRepository.findById(app.getUserId()).orElse(null);
            String email = u != null ? u.getEmail() : null;
            String name = u != null ? u.getFullName() : null;

            return new HrApplicationListItemResponse(
                    app.getId(),
                    job.getId(),
                    app.getUserId(),
                    name,
                    email,
                    app.getAppliedAt(),
                    normalizeStatus(app.getStatus())
            );
        }).toList();
    }

    // GET /api/hr/applications/{applicationId}
    public HrApplicationDetailResponse getApplicationDetail(Long applicationId) {
        JobApplicationEntity app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));

        Opportunity job = requireOwnedJob(app.getOpportunityId());

        User u = userRepository.findById(app.getUserId()).orElse(null);
        String email = u != null ? u.getEmail() : null;
        String name = u != null ? u.getFullName() : null;

        StudentEntity student = null;
        if (email != null) {
            student = studentRepository.findByEmail(email).orElse(null);
        }

        return new HrApplicationDetailResponse(
                app.getId(),
                job.getId(),
                app.getUserId(),
                name,
                email,
                student != null ? student.getSkills() : null,
                student != null ? student.getCareerInterest() : null,
                student != null ? student.getCvText() : null,
                app.getAppliedAt(),
                normalizeStatus(app.getStatus()),
                app.getNotes()
        );
    }

    // PATCH /api/hr/applications/{applicationId}/status
    @Transactional
    public void updateApplicationStatus(Long applicationId, String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status is required");
        }

        JobApplicationEntity app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));

        requireOwnedJob(app.getOpportunityId());

        String s = status.trim().toUpperCase();
        List<String> allowed = List.of("APPLIED", "SHORTLISTED", "INTERVIEW", "REJECTED", "HIRED", "WITHDRAWN");

        if (!allowed.contains(s)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid status (APPLIED, SHORTLISTED, INTERVIEW, REJECTED, HIRED, WITHDRAWN)");
        }

        app.setStatus(s);
        applicationRepository.save(app);
    }

    // POST /api/hr/applications/{applicationId}/notes
    @Transactional
    public void addOrUpdateNote(Long applicationId, String note) {
        if (note == null || note.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "note is required");
        }

        JobApplicationEntity app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));

        requireOwnedJob(app.getOpportunityId());

        String current = app.getNotes() == null ? "" : app.getNotes().trim();
        String updated = current.isEmpty() ? note.trim() : (current + "\n---\n" + note.trim());

        app.setNotes(updated);
        applicationRepository.save(app);
    }

    // ownership check: opportunity.discordId must match current HR userId (stored as string/long)
    private Opportunity requireOwnedJob(Long jobId) {
        Long hrId = SecurityUtil.requireUserId();
        String ownerId = String.valueOf(hrId);

        Opportunity job = opportunityRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));

        Object discord = job.getDiscordId(); // could be String or Long depending on your entity
        String jobOwner = (discord == null) ? null : String.valueOf(discord);

        if (jobOwner == null || !jobOwner.equals(ownerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed");
        }

        return job;
    }

    private String normalizeStatus(String s) {
        if (s == null || s.trim().isEmpty()) return "APPLIED";
        return s.trim().toUpperCase();
    }
}
