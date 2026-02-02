package com.jobifycvut.backend.controller;

import com.jobifycvut.backend.dto.HrApplicationDetailResponse;
import com.jobifycvut.backend.dto.HrApplicationListItemResponse;
import com.jobifycvut.backend.dto.HrApplicationNoteRequest;
import com.jobifycvut.backend.dto.HrApplicationStatusRequest;
import com.jobifycvut.backend.service.HrCandidatePipelineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hr")
public class HrCandidatePipelineController {

    private final HrCandidatePipelineService service;

    public HrCandidatePipelineController(HrCandidatePipelineService service) {
        this.service = service;
    }

    // GET /api/hr/jobs/{jobId}/applications
    @GetMapping("/jobs/{jobId}/applications")
    public ResponseEntity<List<HrApplicationListItemResponse>> listApplicants(@PathVariable Long jobId) {
        return ResponseEntity.ok(service.listApplicantsForJob(jobId));
    }

    // GET /api/hr/applications/{applicationId}
    @GetMapping("/applications/{applicationId}")
    public ResponseEntity<HrApplicationDetailResponse> detail(@PathVariable Long applicationId) {
        return ResponseEntity.ok(service.getApplicationDetail(applicationId));
    }

    // PATCH /api/hr/applications/{applicationId}/status
    @PatchMapping("/applications/{applicationId}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable Long applicationId,
                                             @RequestBody HrApplicationStatusRequest req) {
        service.updateApplicationStatus(applicationId, req.getStatus());
        return ResponseEntity.noContent().build();
    }

    // POST /api/hr/applications/{applicationId}/notes
    @PostMapping("/applications/{applicationId}/notes")
    public ResponseEntity<Void> addNote(@PathVariable Long applicationId,
                                        @RequestBody HrApplicationNoteRequest req) {
        service.addOrUpdateNote(applicationId, req.getNote());
        return ResponseEntity.noContent().build();
    }
}
