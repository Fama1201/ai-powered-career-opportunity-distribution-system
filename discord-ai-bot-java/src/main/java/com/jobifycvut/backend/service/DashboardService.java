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
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardService {

    private final StudentRepository studentRepository;
    private final OpportunityRepository opportunityRepository;
    private final JobApplicationRepository applicationRepository;
    private final SavedJobRepository savedJobRepository;
    private final MatchService matchService;

    public DashboardService(StudentRepository studentRepository,
                            OpportunityRepository opportunityRepository,
                            JobApplicationRepository applicationRepository,
                            SavedJobRepository savedJobRepository,
                            MatchService matchService) {
        this.studentRepository = studentRepository;
        this.opportunityRepository = opportunityRepository;
        this.applicationRepository = applicationRepository;
        this.savedJobRepository = savedJobRepository;
        this.matchService = matchService;
    }

    // 1) OVERVIEW
    public DashboardOverviewResponse getOverview(Long userId, String email) {
        StudentEntity student = findStudent(userId, email);

        String name = (student != null && student.getName() != null && !student.getName().isBlank())
                ? student.getName()
                : "Student";

        long appCount = applicationRepository.countByUserId(userId);
        long savedCount = savedJobRepository.countByUserId(userId);

        List<OpportunityListResponse> matches = getMatches(userId, email).stream()
                .limit(3)
                .toList();

        return new DashboardOverviewResponse(
                name,
                (int) appCount,
                (int) savedCount,
                matches
        );
    }

    // 2) MATCHES - Uses improved MatchService for better scoring
    public List<OpportunityListResponse> getMatches(Long userId, String email) {
        StudentEntity student = findStudent(userId, email);
        if (student == null) return List.of();

        // Use MatchService for intelligent matching
        return matchService.findMatches(student, 10);
    }

    // 3) NEW JOBS
    public List<OpportunityListResponse> getNewJobs() {
        return opportunityRepository.findAll(Sort.by(Sort.Direction.DESC, "id")).stream()
                .limit(5)
                .map(this::mapToResponse)
                .toList();
    }

    // 4) STATS
    public ApplicationStatsResponse getStats(Long userId) {
        long count = applicationRepository.countByUserId(userId);
        return new ApplicationStatsResponse((int) count);
    }

    // 5) PROGRESS
    public ProgressResponse getProgress(Long userId, String email) {
        StudentEntity student = findStudent(userId, email);
        if (student == null) return new ProgressResponse(0, "Profile not created");

        int score = 0;
        if (student.getName() != null && !student.getName().isBlank()) score += 25;
        if (student.getEmail() != null && !student.getEmail().isBlank()) score += 25;
        if (student.getSkills() != null && !student.getSkills().isBlank()) score += 25;
        if (student.getCareerInterest() != null && !student.getCareerInterest().isBlank()) score += 25;

        String msg = score == 100 ? "Profile Complete! 🎉" : "Keep going! 🚀";
        return new ProgressResponse(score, msg);
    }

    // ---- helpers ----

    private StudentEntity findStudent(Long userId, String email) {
        // Primary: match by ID (JWT userId)
        var byId = studentRepository.findById(userId);
        if (byId.isPresent()) return byId.get();

        // Fallback: match by email (if your auth userId differs from student.id)
        if (email != null && !email.isBlank()) {
            return studentRepository.findByEmail(email).orElse(null);
        }
        return null;
    }

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
