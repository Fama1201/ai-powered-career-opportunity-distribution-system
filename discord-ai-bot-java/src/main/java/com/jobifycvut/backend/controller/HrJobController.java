package com.jobifycvut.backend.controller;

import com.jobifycvut.backend.dto.HrJobCreateRequest;
import com.jobifycvut.backend.dto.HrJobStatusRequest;
import com.jobifycvut.backend.model.Opportunity;
import com.jobifycvut.backend.service.HrJobService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hr/jobs")
public class HrJobController {

    private final HrJobService service;

    public HrJobController(HrJobService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Opportunity> create(@RequestBody HrJobCreateRequest req) {
        return ResponseEntity.ok(service.createJob(req));
    }

    @GetMapping
    public ResponseEntity<List<Opportunity>> list(@RequestParam(required = false) String status) {
        return ResponseEntity.ok(service.listMyJobs(status));
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<Opportunity> details(@PathVariable Long jobId) {
        return ResponseEntity.ok(service.getMyJob(jobId));
    }

    @PutMapping("/{jobId}")
    public ResponseEntity<Opportunity> update(@PathVariable Long jobId, @RequestBody HrJobCreateRequest req) {
        return ResponseEntity.ok(service.updateMyJob(jobId, req));
    }

    @PatchMapping("/{jobId}/status")
    public ResponseEntity<Void> patchStatus(@PathVariable Long jobId, @RequestBody HrJobStatusRequest req) {
        service.updateStatus(jobId, req.getStatus());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{jobId}")
    public ResponseEntity<Void> delete(@PathVariable Long jobId) {
        service.deleteJob(jobId);
        return ResponseEntity.noContent().build();
    }
}
