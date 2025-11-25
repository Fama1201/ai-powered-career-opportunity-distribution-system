package com.jobifycvut.backend.controller;

import storage.OpportunityDAO;
import bot.api.OpportunityClient.Opportunity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final OpportunityDAO opportunityDAO;

    @Autowired
    public JobController(OpportunityDAO opportunityDAO) {
        this.opportunityDAO = opportunityDAO;
    }

    /**
     * GET /api/jobs
     * Returns all available jobs with pagination.
     * Usage: /api/jobs?page=1&size=20
     */
    @GetMapping
    public List<Opportunity> getAllJobs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return opportunityDAO.findAllPaginated(page, size);
    }

    /**
     * GET /api/jobs/search
     * Search jobs by keywords (title or description).
     * Usage: /api/jobs/search?keyword=Java
     */
    @GetMapping("/search")
    public List<Opportunity> searchJobs(@RequestParam String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of(); // Return empty if no keyword
        }
        return opportunityDAO.searchByKeyword(keyword);
    }

    /**
     * GET /api/jobs/filters
     * Get available filter options (static for now).
     */
    @GetMapping("/filters")
    public Map<String, List<String>> getFilterOptions() {
        return Map.of(
                "job_types", List.of("Full-time", "Part-time", "Internship", "Contract"),
                "work_models", List.of("Remote", "On-site", "Hybrid")
        );
    }

    /**
     * GET /api/jobs/categories
     * Get job categories list.
     */
    @GetMapping("/categories")
    public List<String> getCategories() {
        // You can make this dynamic later by querying 'DISTINCT job_type' from DB
        return List.of("Software Engineering", "Data Science", "DevOps", "Product Management", "Design");
    }

    /**
     * GET /api/jobs/{jobId}/details
     * Get full job description and requirements for a specific job.
     */
    @GetMapping("/{jobId}/details")
    public ResponseEntity<Opportunity> getJobDetails(@PathVariable String jobId) {
        Opportunity opp = opportunityDAO.findById(jobId);
        if (opp != null) {
            return ResponseEntity.ok(opp);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}