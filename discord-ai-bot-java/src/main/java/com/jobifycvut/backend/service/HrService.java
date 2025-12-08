package com.jobifycvut.backend.service;

import java.util.UUID;
import com.jobifycvut.backend.dto.HrRegisterRequest;
import com.jobifycvut.backend.dto.HrLoginRequest;
import com.jobifycvut.backend.model.HrUser;
import com.jobifycvut.backend.repository.HrUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

/**
 * Service layer class containing the core business logic for HR user management and authentication.
 * Handles tasks like password hashing, data validation, and session verification.
 */
@Service
@RequiredArgsConstructor // Automatically injects final dependencies (Repository, Encoder)
public class HrService {

    // Injected dependencies via Lombok's @RequiredArgsConstructor
    private final HrUserRepository hrRepository;
    private final PasswordEncoder passwordEncoder; // Used for secure password hashing (BCrypt)

    /**
     * Handles the registration process for a new HR user.
     * @param request The registration data (raw password, email, names).
     */
    public void register(HrRegisterRequest request) {
        // 1. Validation: Check if the email already exists in the database.
        if (hrRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        // 2. Mapping DTO to Entity (HrUser)
        HrUser hr = new HrUser();
        hr.setEmail(request.getEmail());
        hr.setFullName(request.getFullName());
        hr.setCompanyName(request.getCompanyName());

        // 3. Security: Hash the raw password before saving it.
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        hr.setPasswordHash(encodedPassword);

        // 4. Set Metadata and Tokens
        hr.setCreatedAt(LocalDateTime.now());
        // Generates a unique ID (UUID) for internal tracking/verification links.
        hr.setHrTokens(UUID.randomUUID().toString());

        // 5. Save the new user entity to the database.
        hrRepository.save(hr);
    }

    /**
     * Handles the user login process by verifying credentials.
     * @param request The login data (email and raw password).
     * @return The authenticated HrUser entity.
     */
    public HrUser login(HrLoginRequest request) {
        // 1. Lookup: Find the user by email. Throws exception if user is not found.
        HrUser user = hrRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Not found"));

        // 2. Verification: Check if the raw password matches the stored hash.
        // passwordEncoder.matches() handles the BCrypt comparison securely.
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid Credentials");
        }

        // 3. Return the user (ready for token generation in the Controller).
        return user;
    }
}