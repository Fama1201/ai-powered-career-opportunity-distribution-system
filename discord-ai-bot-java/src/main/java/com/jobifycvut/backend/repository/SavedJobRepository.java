package com.jobifycvut.backend.repository;
import com.jobifycvut.backend.model.SavedJob;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavedJobRepository extends JpaRepository<SavedJob, Long> {
    boolean existsByUserIdAndOpportunityId(Long userId, Long opportunityId);
}
