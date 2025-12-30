package com.jobifycvut.backend.repository;

import com.jobifycvut.backend.model.StudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Data Access Layer (DAO).
 * This interface handles all database communication (Save, Find, Delete).
 * We extend JpaRepository so Spring Boot implements the methods for us automatically.
 */
@Repository
public interface StudentRepository extends JpaRepository<StudentEntity, Long> {

    /**
     * Custom method to find a student using their unique Discord ID.
     * * Spring Boot performs "Magic" here: it reads the method name 'findByDiscordId'
     * and automatically generates the SQL query:
     * SELECT * FROM student WHERE discord_id = ?
     * * @param discordId The ID to search for.
     * @return Optional (A container that may or may not contain the student).
     */
    Optional<StudentEntity> findByDiscordId(String discordId);
}