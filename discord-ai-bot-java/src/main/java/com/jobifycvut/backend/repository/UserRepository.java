package com.jobifycvut.backend.repository;

import com.jobifycvut.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByPasswordResetToken(String password);
    Optional<User> findByEmailVerificationToken(String emailVerificationToken);

}