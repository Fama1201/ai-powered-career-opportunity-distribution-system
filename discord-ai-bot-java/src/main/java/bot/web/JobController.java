package bot.web;

import bot.storage.OpportunityDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import bot.storage.Opportunity;

/**
 * Handles all API requests for the Job Browser page.
 * This is the main backend component for the student's job search UI.
 */
@RestController
@RequestMapping("/api/jobs")
public class JobController {

    @Autowired
    private OpportunityDAO opportunityDAO;

    /**
     * GET /api/jobs
     * Get all available jobs with pagination and filtering.
     * @param page       The current page number (e.g., ?page=1)
     * @param keyword    A search keyword (e.g., ?keyword=java)
     * @param categories A list of categories to filter by (e.g., ?categories=Full-time,Part-time)
     * @return A paginated list of jobs.
     */
    @GetMapping
    public Page<Opportunity> getAllJobs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<String> categories) {

        // Spring page numbers are 0-indexed, so we subtract 1
        Pageable pageable = PageRequest.of(page - 1, 10); // 10 results per page

        // Build a dynamic query using Specifications
        Specification<Opportunity> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Add keyword search (searches title, description, and tech requirements)
            if (keyword != null && !keyword.isBlank()) {
                String likePattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), likePattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), likePattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("technicalRequirements")), likePattern)
                ));
            }

            // 2. Add category filter (assuming categories are job_type)
            if (categories != null && !categories.isEmpty()) {
                predicates.add(root.get("jobType").in(categories));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        // Run the query and return the page
        return opportunityDAO.findAll(spec, pageable);
    }

    /**
     * GET /api/jobs/search
     * A simple search endpoint that finds jobs by keyword in the title.
     * @param keyword The word to search for.
     * @return A list of matching jobs.
     */
    @GetMapping("/search")
    public List<Opportunity> searchJobs(@RequestParam String keyword) {
        String normalizedKeyword = keyword.trim();
        List<Opportunity> matchingOpportunities =
                opportunityDAO.findByTitleContainingIgnoreCase(normalizedKeyword);
        return matchingOpportunities;
    }

    /**
     * GET /api/jobs/filters
     * Get all available filter options (e.g., job types, work models).
     * @return A map of available filters.
     */
    @GetMapping("/filters")
    public Map<String, List<String>> getFilterOptions() {
        // Now returns real, dynamic data from the database
        List<String> jobTypes = opportunityDAO.findDistinctJobTypes();
        List<String> workModels = opportunityDAO.findDistinctHomeOffice();

        return Map.of(
                "job_types", jobTypes,
                "work_models", workModels
        );
    }

    /**
     * GET /api/jobs/categories
     * Get all job categories. (Assumes categories are job_types)
     * @return A list of categories.
     */
    @GetMapping("/categories")
    public List<String> getCategories() {
        // Returns all unique job types from the opportunities table
        return opportunityDAO.findDistinctJobTypes();
    }

    /**
     * GET /api/jobs/{jobId}/details
     * Get the full details for a single job.
     * @param jobId The ID of the job.
     * @return The full job details.
     */
    @GetMapping("/{jobId}/details")
    public ResponseEntity<Opportunity> getJobDetails(@PathVariable Integer jobId) {
        // Finds the job by its ID.
        // If not found, returns a 404 Not Found status.
        return opportunityDAO.findById(jobId)
                .map(ResponseEntity::ok) // If found, wrap in 200 OK
                .orElse(ResponseEntity.notFound().build()); // If not found, return 404
    }
}