package com.jobifycvut.backend.controller;

import com.jobifycvut.backend.dto.ApplicationResponse;
import com.jobifycvut.backend.service.ApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class JobApplicationController {

    private final ApplicationService service;

    public JobApplicationController(ApplicationService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ApplicationResponse>> getApplications(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long opportunityId,
            @RequestParam(required = false) String status
    ) {
        return ResponseEntity.ok(service.getApplications(userId, opportunityId, status));
    }

    @GetMapping("/{applicationId}")
    public ResponseEntity<ApplicationResponse> getById(@PathVariable Long applicationId) {
        return ResponseEntity.ok(service.getById(applicationId));
    }

    @PutMapping("/{applicationId}/withdraw")
    public ResponseEntity<ApplicationResponse> withdraw(@PathVariable Long applicationId) {
        return ResponseEntity.ok(service.withdraw(applicationId));
    }
}
