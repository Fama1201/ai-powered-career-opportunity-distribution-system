package com.jobifycvut.backend.controller;

import com.jobifycvut.backend.dto.ApiResponse;
import com.jobifycvut.backend.dto.OpportunityDetailResponse;
import com.jobifycvut.backend.dto.OpportunityListResponse;
import com.jobifycvut.backend.exception.AuthException;
import com.jobifycvut.backend.service.OpportunityService;
import com.jobifycvut.backend.util.JwtUtil;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/api/jobs")
public class OpportunityController {

    private final  OpportunityService opportunityService;
    public OpportunityController(OpportunityService opportunityService) {
        this.opportunityService = opportunityService;
    }
    //get api/jobs
    @GetMapping
    public Page<OpportunityListResponse> getOpportunities(Pageable pageable) {
        return opportunityService.getAllOpportunities(pageable);
    }

    //get api/jobs/{jobId} in details
    @GetMapping("/{id}")
    public OpportunityDetailResponse getOpportunityById(@PathVariable Long id,
                                                        @RequestParam(required = false) Long userId) {


        return opportunityService.getOpportunityById(id);
    }

    // get api/jobs/search?keyword=backend
    @GetMapping("/search")
    public Page<OpportunityListResponse> searchOpportunities(@RequestParam String keyword, Pageable pageable) {
        return opportunityService.searchOpportunities(keyword, pageable);
    }

    //get api/jobs/{id}/saved
    @PostMapping("/{id}/save")
    public ResponseEntity<ApiResponse>saveJob(@PathVariable Long id, @RequestHeader(value = "Authorization",  required = false) String authorizationHeader) {        Long userId = extractUserIdFromAuthHeader(authorizationHeader);
        System.out.println("saveJob -> userId = " + userId);
        opportunityService.saveOpportunity(id, userId);
        return ResponseEntity.ok(new ApiResponse("Job saved successfully"));
    }

    //get api/jobs/{id}/apply
    @PostMapping("/{id}/apply")
    public ResponseEntity<ApiResponse>applyJob(@PathVariable Long id,
                                               @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        Long userId = extractUserIdFromAuthHeader(authorizationHeader);
        System.out.println("applyJob -> userId = " + userId);
        opportunityService.applyToOpportunity(id, userId);
        return ResponseEntity.ok(new ApiResponse("Job applied successfully"));
    }


    private Long extractUserIdFromAuthHeader(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new AuthException("Missing or invalid Authorization header");
        }

        String token = authorizationHeader.substring(7); // remove "Bearer "
        Map<String, Object> claims = JwtUtil.validateToken(token);

        if (claims == null) {
            throw new AuthException("Invalid or expired token");
        }

        Object userIdObj = claims.get("userId");
        if (userIdObj == null) {
            throw new AuthException("Token does not contain userId");
        }

        return ((Number) userIdObj).longValue();
    }

}
