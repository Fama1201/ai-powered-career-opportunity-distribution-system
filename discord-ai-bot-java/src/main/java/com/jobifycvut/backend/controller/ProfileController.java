package com.jobifycvut.backend.controller;

import com.jobifycvut.backend.model.StudentEntity;
import com.jobifycvut.backend.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for accessing raw Student profiles.
 * This class handles HTTP requests coming from the Frontend (React).
 */
@RestController // Marks this class as a REST API handler (returns JSON)
@RequestMapping("/api") // All URLs in this class start with /api
public class ProfileController {

    @Autowired // Spring automatically injects the Repository here (Dependency Injection)
    private StudentRepository studentRepository;

    /**
     * Endpoint: GET /api/students
     * Returns a list of ALL students in the database.
     * Useful for testing if the database connection works properly.
     */
    @GetMapping("/students")
    public List<StudentEntity> getAllStudents() {
        return studentRepository.findAll();
    }

    /**
     * Endpoint: GET /api/profile/{discordId}
     * Returns a single student profile based on the Discord ID passed in the URL.
     * Example usage: /api/profile/988228291183902750
     * * @param discordId The ID extracted from the URL path.
     * @return HTTP 200 OK with student data, or HTTP 404 Not Found if missing.
     */
    @GetMapping("/profile/{discordId}")
    public ResponseEntity<StudentEntity> getProfile(@PathVariable String discordId) {
        // Look for the student by Discord ID
        return studentRepository.findByDiscordId(discordId)
                .map(student -> ResponseEntity.ok().body(student)) // If found -> Return 200 OK
                .orElse(ResponseEntity.notFound().build());      // If empty -> Return 404 Not Found
    }
}