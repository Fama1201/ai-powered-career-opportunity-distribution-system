package com.jobifycvut.backend.repository;

import com.jobifycvut.backend.model.Opportunity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface OpportunityRepository extends JpaRepository<Opportunity, Long> {

    Page<Opportunity> findByTitleContainingIgnoreCaseOrCompanyContainingIgnoreCase(
            String titleKeyword, String companyKeyword, Pageable pageable
    );

    @Query("""
        SELECT o FROM Opportunity o
        WHERE (:jobType IS NULL OR :jobType = '' OR lower(coalesce(o.jobType, '')) LIKE lower(concat('%', :jobType, '%')))
          AND (:workMode IS NULL OR :workMode = '' OR lower(coalesce(o.homeOffice, '')) LIKE lower(concat('%', :workMode, '%')))
        """)
    Page<Opportunity> findAllWithFilters(
            @Param("jobType") String jobType,
            @Param("workMode") String workMode,
            Pageable pageable
    );

    @Query("""
        SELECT o FROM Opportunity o
        WHERE (lower(coalesce(o.title, '')) LIKE lower(concat('%', :keyword, '%'))
            OR lower(coalesce(o.company, '')) LIKE lower(concat('%', :keyword, '%')))
          AND (:jobType IS NULL OR :jobType = '' OR lower(coalesce(o.jobType, '')) LIKE lower(concat('%', :jobType, '%')))
          AND (:workMode IS NULL OR :workMode = '' OR lower(coalesce(o.homeOffice, '')) LIKE lower(concat('%', :workMode, '%')))
        """)
    Page<Opportunity> searchWithFilters(
            @Param("keyword") String keyword,
            @Param("jobType") String jobType,
            @Param("workMode") String workMode,
            Pageable pageable
    );

    List<Opportunity> findByDiscordIdOrderByIdDesc(String discordId);

    List<Opportunity> findByDiscordIdAndApplicationDeadlineGreaterThanEqualOrderByIdDesc(String discordId, LocalDate date);

    List<Opportunity> findByDiscordIdAndApplicationDeadlineLessThanOrderByIdDesc(String discordId, LocalDate date);

    List<Opportunity> findByDiscordIdAndApplicationDeadlineIsNullOrderByIdDesc(String discordId);

    java.util.Optional<Opportunity> findByOpportunityIdAndDiscordId(String opportunityId, String discordId);
}
