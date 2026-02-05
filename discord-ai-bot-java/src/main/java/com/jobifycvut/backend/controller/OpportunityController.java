package com.jobifycvut.backend.controller;

import com.jobifycvut.backend.dto.ApiResponse;
import com.jobifycvut.backend.dto.OpportunityDetailResponse;
import com.jobifycvut.backend.dto.OpportunityListResponse;
import com.jobifycvut.backend.security.SecurityUtil;
import com.jobifycvut.backend.service.OpportunityService;
import com.jobifycvut.backend.service.OpportunitySyncService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jobs")
public class OpportunityController {

    private final OpportunityService opportunityService;
    private final OpportunitySyncService opportunitySyncService;

    public OpportunityController(OpportunityService opportunityService,
                                 OpportunitySyncService opportunitySyncService) {
        this.opportunityService = opportunityService;
        this.opportunitySyncService = opportunitySyncService;
    }

    // GET /api/jobs
    @GetMapping
    public Page<OpportunityListResponse> getOpportunities(
            @RequestParam(required = false) String jobType,
            @RequestParam(required = false) String workMode,
            Pageable pageable
    ) {
        return opportunityService.getAllOpportunities(pageable, jobType, workMode);
    }

    // GET /api/jobs/{jobId}
    @GetMapping("/{id}")
    public OpportunityDetailResponse getOpportunityById(@PathVariable Long id) {
        Long userId = SecurityUtil.requireUserId();
        return opportunityService.getOpportunityById(id, userId);
    }


    // GET /api/jobs/search?keyword=backend
    @GetMapping("/search")
    public Page<OpportunityListResponse> searchOpportunities(
            @RequestParam String keyword,
            @RequestParam(required = false) String jobType,
            @RequestParam(required = false) String workMode,
            Pageable pageable
    ) {
        return opportunityService.searchOpportunities(keyword, pageable, jobType, workMode);
    }

    // POST /api/jobs/{id}/save
    @PostMapping("/{id}/save")
    public ResponseEntity<ApiResponse> saveJob(@PathVariable Long id) {
        Long userId = SecurityUtil.requireUserId();
        opportunityService.saveOpportunity(id, userId);
        return ResponseEntity.ok(new ApiResponse("Job saved successfully"));
    }

    // POST /api/jobs/{id}/apply
    @PostMapping("/{id}/apply")
    public ResponseEntity<ApiResponse> applyJob(@PathVariable Long id) {
        Long userId = SecurityUtil.requireUserId();
        opportunityService.applyToOpportunity(id, userId);
        return ResponseEntity.ok(new ApiResponse("Job applied successfully"));
    }

    // POST /api/jobs/sync
    @PostMapping("/sync")
    public ResponseEntity<ApiResponse> syncJobs() {
        int count = opportunitySyncService.syncFromEdumatch();
        return ResponseEntity.ok(new ApiResponse("Synced " + count + " opportunities."));
    }
}
