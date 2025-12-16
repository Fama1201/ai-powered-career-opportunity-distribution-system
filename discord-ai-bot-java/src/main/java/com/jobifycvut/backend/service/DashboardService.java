package com.jobifycvut.backend.service;

import com.jobifycvut.backend.dto.ApplicationStatsResponse;
import com.jobifycvut.backend.dto.DashboardOverviewResponse;
import com.jobifycvut.backend.dto.OpportunityListResponse;
import com.jobifycvut.backend.dto.ProgressResponse;
import com.jobifycvut.backend.model.Opportunity;
import com.jobifycvut.backend.model.StudentEntity;
import com.jobifycvut.backend.repository.JobApplicationRepository;
import com.jobifycvut.backend.repository.OpportunityRepository;
import com.jobifycvut.backend.repository.SavedJobRepository;
import com.jobifycvut.backend.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service to handle Student Dashboard logic.
 * Aggregates data from multiple repositories.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final StudentRepository studentRepository;
    private final OpportunityRepository opportunityRepository;
    private final JobApplicationRepository applicationRepository;
    private final SavedJobRepository savedJobRepository; // You need to add countByUserId in repo if not exists

    // 1. GET OVERVIEW
    public DashboardOverviewResponse getOverview(Long userId, String discordId) {
        // Fetch student name
        StudentEntity student = studentRepository.findByDiscordId(discordId)
                .orElse(new StudentEntity()); // Return empty if not found

        // Count applications (assuming you have a method countByUserId in JobApplicationRepository)
        // If not, we will add it to the Repo in next steps, for now let's assume 0 or fetch list size
        long appCount = applicationRepository.count(); // TODO: Filter by userId properly later

        // Fetch recent 3 matches
        List<OpportunityListResponse> matches = getMatches(discordId).stream().limit(3).collect(Collectors.toList());

        return new DashboardOverviewResponse(
                student.getName() != null ? student.getName() : "Student",
                (int) appCount, // Cast for now
                0, // Placeholder for saved jobs
                matches
        );
    }

    // 2. GET MATCHES (Based on Career Interest)
    public List<OpportunityListResponse> getMatches(String discordId) {
        StudentEntity student = studentRepository.findByDiscordId(discordId)
                .orElseThrow(() -> new RuntimeException("Student profile not found"));

        String interest = student.getCareerInterest();
        if (interest == null || interest.isEmpty()) {
            return List.of(); // No interest = No matches
        }

        // Search opportunities containing the career interest (e.g., "Java", "Python")
        // We use the existing repository method
        return opportunityRepository.findByTitleContainingIgnoreCaseOrCompanyContainingIgnoreCase(interest, interest, PageRequest.of(0, 10))
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // 3. GET NEW JOBS (Last 5 added)
    public List<OpportunityListResponse> getNewJobs() {
        return opportunityRepository.findAll(Sort.by(Sort.Direction.DESC, "id")).stream()
                .limit(5)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // 4. GET STATS
    public ApplicationStatsResponse getStats(Long userId) {
        // In a real scenario: applicationRepository.countByUserId(userId)
        long count = applicationRepository.count();
        return new ApplicationStatsResponse((int) count);
    }

    // 5. GET PROGRESS
    public ProgressResponse getProgress(String discordId) {
        StudentEntity student = studentRepository.findByDiscordId(discordId)
                .orElse(null);

        if (student == null) return new ProgressResponse(0, "Profile not created");

        int score = 0;
        if (student.getName() != null) score += 25;
        if (student.getEmail() != null) score += 25;
        if (student.getSkills() != null) score += 25;
        if (student.getCareerInterest() != null) score += 25;

        String msg = score == 100 ? "Profile Complete! 🎉" : "Keep going! 🚀";
        return new ProgressResponse(score, msg);
    }

    // Helper to convert Entity -> DTO
    private OpportunityListResponse mapToResponse(Opportunity opp) {
        return new OpportunityListResponse(
                opp.getId(),
                opp.getTitle(),
                opp.getCompany(),
                opp.getJobType(),
                opp.getHomeOffice(),
                opp.getWage(),
                opp.getApplicationDeadline()
        );
    }
}