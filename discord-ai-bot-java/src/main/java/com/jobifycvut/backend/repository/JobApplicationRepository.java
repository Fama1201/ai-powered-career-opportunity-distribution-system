package com.jobifycvut.backend.repository;

import com.jobifycvut.backend.model.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    boolean existsByUserIdAndOpportunityId(Long userId, Long opportunityId);
}
