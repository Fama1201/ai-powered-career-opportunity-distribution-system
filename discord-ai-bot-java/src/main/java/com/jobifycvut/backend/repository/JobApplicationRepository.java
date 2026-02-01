package com.jobifycvut.backend.repository;

import com.jobifycvut.backend.model.JobApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobApplicationRepository extends JpaRepository<JobApplicationEntity, Long> {
    List<JobApplicationEntity> findByUserId(Long userId);
    List<JobApplicationEntity> findByStatusIgnoreCase(String status);
    List<JobApplicationEntity> findByOpportunityId(Long opportunityId);
    List<JobApplicationEntity> findByUserIdAndOpportunityId(Long userId,  Long opportunityId);
    List<JobApplicationEntity> findByUserIdAndStatusIgnoreCase(Long userId, String status);

}
