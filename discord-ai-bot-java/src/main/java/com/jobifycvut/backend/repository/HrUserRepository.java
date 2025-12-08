package com.jobifycvut.backend.repository;

import com.jobifycvut.backend.model.HrUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for managing HrUser entities (the 'hr' table).
 * Extends JpaRepository, providing standard CRUD operations.
 * * NOTE: The primary key type is set to Long, ensure this matches the 'id' type
 * in the HrUser entity and the corresponding database column.
 */
@Repository
public interface HrUserRepository extends JpaRepository<HrUser, Long> {

    /**
     * Spring Data JPA method derived from the property name.
     * Searches for a user by their email address.
     * * @param email The email address to search for.
     * @return An Optional containing the HrUser if found.
     */
    Optional<HrUser> findByEmail(String email);

    /**
     * Spring Data JPA method to quickly check if a user with the given email already exists.
     * This is typically used during the registration process to enforce uniqueness.
     * * @param email The email address to check.
     * @return True if a record with this email exists, false otherwise.
     */
    boolean existsByEmail(String email);
}