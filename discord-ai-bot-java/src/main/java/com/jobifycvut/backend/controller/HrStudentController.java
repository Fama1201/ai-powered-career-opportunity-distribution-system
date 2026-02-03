package com.jobifycvut.backend.controller;

import com.jobifycvut.backend.dto.CvResponse;
import com.jobifycvut.backend.dto.StudentProfileResponse;
import com.jobifycvut.backend.service.HrStudentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/hr/students")
public class HrStudentController {

    private final HrStudentService hrStudentService;

    public HrStudentController(HrStudentService hrStudentService) {
        this.hrStudentService = hrStudentService;
    }

    // GET /api/hr/students/{studentId}/profile
    @GetMapping("/{studentId}/profile")
    public ResponseEntity<StudentProfileResponse> getStudentProfile(@PathVariable Long studentId) {
        return ResponseEntity.ok(hrStudentService.getProfile(studentId));
    }

    // GET /api/hr/students/{studentId}/cv
    @GetMapping("/{studentId}/cv")
    public ResponseEntity<CvResponse> getStudentCv(@PathVariable Long studentId) {
        return ResponseEntity.ok(hrStudentService.getCv(studentId));
    }

    // GET /api/hr/students/search?skills=&major=&gpa=&keywords=
    @GetMapping("/search")
    public ResponseEntity<List<StudentProfileResponse>> searchStudents(
            @RequestParam(required = false) String skills,
            @RequestParam(required = false) String major,
            @RequestParam(required = false) String gpa,
            @RequestParam(required = false) String keywords
    ) {
        // Keep same behavior: GPA not supported yet
        if (gpa != null && !gpa.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "gpa filter is not supported yet");
        }

        // Normalize inputs (safe trim)
        String skillsQ = safeTrim(skills);
        String majorQ = safeTrim(major);
        String keywordsQ = safeTrim(keywords);

        return ResponseEntity.ok(hrStudentService.search(skillsQ, majorQ, keywordsQ));
    }

    private String safeTrim(String s) {
        return s == null ? null : s.trim();
    }
}
