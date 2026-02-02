package com.jobifycvut.backend.repository;

import com.jobifycvut.backend.model.Opportunity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface OpportunityRepository extends JpaRepository<Opportunity, Long> {

    Page<Opportunity> findByTitleContainingIgnoreCaseOrCompanyContainingIgnoreCase(
            String titleKeyword, String companyKeyword, Pageable pageable
    );

    List<Opportunity> findByDiscordIdOrderByIdDesc(String discordId);

    List<Opportunity> findByDiscordIdAndApplicationDeadlineGreaterThanEqualOrderByIdDesc(String discordId, LocalDate date);

    List<Opportunity> findByDiscordIdAndApplicationDeadlineLessThanOrderByIdDesc(String discordId, LocalDate date);

    List<Opportunity> findByDiscordIdAndApplicationDeadlineIsNullOrderByIdDesc(String discordId);
}
