package com.jobifycvut.backend.controller;

import com.jobifycvut.backend.security.SecurityUtil;
import com.jobifycvut.backend.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/overview")
    public ResponseEntity<?> overview() {
        var p = SecurityUtil.getPrincipal();
        return ResponseEntity.ok(dashboardService.getOverview(p.getUserId(), p.getEmail()));
    }

    @GetMapping("/matches")
    public ResponseEntity<?> matches() {
        var p = SecurityUtil.getPrincipal();
        return ResponseEntity.ok(dashboardService.getMatches(p.getUserId(), p.getEmail()));
    }

    @GetMapping("/jobs/new")
    public ResponseEntity<?> newJobs() {
        return ResponseEntity.ok(dashboardService.getNewJobs());
    }

    @GetMapping("/applications/stats")
    public ResponseEntity<?> stats() {
        Long userId = SecurityUtil.requireUserId();
        return ResponseEntity.ok(dashboardService.getStats(userId));
    }

    @GetMapping("/progress")
    public ResponseEntity<?> progress() {
        var p = SecurityUtil.getPrincipal();
        return ResponseEntity.ok(dashboardService.getProgress(p.getUserId(), p.getEmail()));
    }
}
