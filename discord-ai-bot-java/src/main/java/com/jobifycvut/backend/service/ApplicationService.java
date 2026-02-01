package com.jobifycvut.backend.service;

import com.jobifycvut.backend.dto.ApplicationResponse;
import com.jobifycvut.backend.model.JobApplicationEntity;
import com.jobifycvut.backend.repository.JobApplicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApplicationService {

    private final JobApplicationRepository repo;

    public ApplicationService(JobApplicationRepository repo) {
        this.repo = repo;
    }

    public List<ApplicationResponse> getApplications(Long userId, Long opportunityId, String status) {
        List<JobApplicationEntity> apps;

        if (userId != null && opportunityId != null) {
            apps = repo.findByUserIdAndOpportunityId(userId, opportunityId);

        } else if (opportunityId != null) {
            apps = repo.findByOpportunityId(opportunityId);

        } else if (userId != null) {
            apps = repo.findByUserId(userId);

        } else if (status != null && !status.trim().isEmpty()) {
            apps = repo.findByStatusIgnoreCase(status);

        } else {
            apps = repo.findAll();
        }

        return apps.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ApplicationResponse getById(Long id) {
        JobApplicationEntity app = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + id));
        return toResponse(app);
    }

    @Transactional
    public ApplicationResponse withdraw(Long id) {
        JobApplicationEntity app = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + id));

        String current = app.getStatus() == null ? "" : app.getStatus().trim().toUpperCase();

        if (!"WITHDRAWN".equals(current)) {
            app.setStatus("WITHDRAWN");
        }

        return toResponse(app);
    }

    private ApplicationResponse toResponse(JobApplicationEntity e) {
        String effectiveStatus =
                (e.getStatus() == null || e.getStatus().trim().isEmpty())
                        ? "APPLIED"
                        : e.getStatus();

        return ApplicationResponse.from(
                e.getId(),
                e.getUserId(),
                e.getOpportunityId(),
                e.getAppliedAt(),
                effectiveStatus,
                e.getNotes()
        );
    }
}
