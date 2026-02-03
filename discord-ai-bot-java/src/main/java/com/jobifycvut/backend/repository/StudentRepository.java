package com.jobifycvut.backend.repository;

import com.jobifycvut.backend.model.StudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<StudentEntity, Long> {

    Optional<StudentEntity> findByDiscordId(String discordId);

    Optional<StudentEntity> findByEmail(String email);

    @Query("""
    SELECT s
    FROM StudentEntity s
    WHERE (:skills IS NULL OR :skills = '' OR LOWER(COALESCE(s.skills, '')) LIKE LOWER(CONCAT('%', :skills, '%')))
      AND (:major IS NULL OR :major = '' OR LOWER(COALESCE(s.careerInterest, '')) LIKE LOWER(CONCAT('%', :major, '%')))
      AND (
           :keywords IS NULL OR :keywords = ''
           OR LOWER(COALESCE(s.name, '')) LIKE LOWER(CONCAT('%', :keywords, '%'))
           OR LOWER(COALESCE(s.email, '')) LIKE LOWER(CONCAT('%', :keywords, '%'))
           OR LOWER(COALESCE(s.skills, '')) LIKE LOWER(CONCAT('%', :keywords, '%'))
           OR LOWER(COALESCE(s.careerInterest, '')) LIKE LOWER(CONCAT('%', :keywords, '%'))
      )
    """)
    List<StudentEntity> search(
            @Param("skills") String skills,
            @Param("major") String major,
            @Param("keywords") String keywords
    );

    List<StudentEntity> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrSkillsContainingIgnoreCaseOrCareerInterestContainingIgnoreCase(
            String name, String email, String skills, String careerInterest
    );
}
