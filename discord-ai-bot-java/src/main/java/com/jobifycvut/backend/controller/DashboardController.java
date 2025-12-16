package com.jobifycvut.backend.controller;

import com.jobifycvut.backend.dto.ApplicationStatsResponse;
import com.jobifycvut.backend.dto.DashboardOverviewResponse;
import com.jobifycvut.backend.dto.OpportunityListResponse;
import com.jobifycvut.backend.dto.ProgressResponse;
import com.jobifycvut.backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for the Student Dashboard.
 * Provides endpoints for overview, matches, new jobs, and stats.
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    // 1. GET /api/dashboard/overview?discordId=...
    @GetMapping("/overview")
    public ResponseEntity<DashboardOverviewResponse> getOverview(@RequestParam String discordId) {
        // In a real app, userId comes from the JWT token. For now, we pass discordId via param.
        return ResponseEntity.ok(dashboardService.getOverview(1L, discordId));
    }

    // 2. GET /api/dashboard/matches?discordId=...
    @GetMapping("/matches")
    public ResponseEntity<List<OpportunityListResponse>> getMatches(@RequestParam String discordId) {
        return ResponseEntity.ok(dashboardService.getMatches(discordId));
    }

    // 3. GET /api/dashboard/jobs/new
    @GetMapping("/jobs/new")
    public ResponseEntity<List<OpportunityListResponse>> getNewJobs() {
        return ResponseEntity.ok(dashboardService.getNewJobs());
    }

    // 4. GET /api/dashboard/applications/stats
    @GetMapping("/applications/stats")
    public ResponseEntity<ApplicationStatsResponse> getStats() {
        // Passing dummy userId 1L for now until we link full JWT student auth
        return ResponseEntity.ok(dashboardService.getStats(1L));
    }

    // 5. GET /api/dashboard/progress?discordId=...
    @GetMapping("/progress")
    public ResponseEntity<ProgressResponse> getProgress(@RequestParam String discordId) {
        return ResponseEntity.ok(dashboardService.getProgress(discordId));
    }
}