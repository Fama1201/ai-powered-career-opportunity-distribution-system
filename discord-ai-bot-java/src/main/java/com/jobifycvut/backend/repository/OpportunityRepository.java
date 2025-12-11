package com.jobifycvut.backend.repository;

import com.jobifycvut.backend.model.Opportunity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OpportunityRepository extends JpaRepository<Opportunity, Long> {
    Page<Opportunity> findByTitleContainingIgnoreCaseOrCompanyContainingIgnoreCase(
            String titleKeyword, String companyKeyword, Pageable pageable
    );
}
