package com.jobifycvut.backend.controller;

import com.jobifycvut.backend.dto.CvResponse;
import com.jobifycvut.backend.dto.CvUpdateRequest;
import com.jobifycvut.backend.model.StudentEntity;
import com.jobifycvut.backend.repository.StudentRepository;
import com.jobifycvut.backend.service.StudentContextService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/cv")
public class CvController {

    private final StudentContextService service;
    private final StudentRepository repository;

    public CvController(StudentContextService service, StudentRepository repository) {
        this.service = service;
        this.repository = repository;
    }

    // GET /api/cv
    @GetMapping
    public ResponseEntity<CvResponse> getCv() {
        StudentEntity student = service.getOrCreateCurrentStudent();
        String cvText = student.getCvText();
        boolean hasCv = cvText != null && !cvText.trim().isEmpty();
        return ResponseEntity.ok(new CvResponse(hasCv, cvText));
    }

    // POST /api/cv/upload
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CvResponse> uploadCv(
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart(value = "extractedText", required = false) String extractedText
    ) throws Exception {

        StudentEntity student = service.getOrCreateCurrentStudent();
        String finalText = null;

        // 1) Prefer extractedText if provided
        if (extractedText != null && !extractedText.trim().isEmpty()) {
            finalText = extractedText.trim();
        }

        // 2) Otherwise allow uploading a .txt file
        if (finalText == null && file != null && !file.isEmpty()) {

            long maxBytes = 5L * 1024L * 1024L; // 5MB
            if (file.getSize() > maxBytes) {
                return ResponseEntity.badRequest()
                        .body(new CvResponse(false, "CV file too large (max 5MB)."));
            }

            String contentType = file.getContentType() != null ? file.getContentType() : "";
            String filename = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";

            boolean isTxt = contentType.contains("text/plain") || filename.endsWith(".txt");
            if (!isTxt) {
                return ResponseEntity.badRequest().body(
                        new CvResponse(false,
                                "Unsupported file type. Upload a .txt file OR send 'extractedText' (recommended for PDF/DOCX).")
                );
            }

            finalText = new String(file.getBytes(), StandardCharsets.UTF_8).trim();
        }

        // 3) Validate we got something
        if (finalText == null || finalText.isEmpty()) {
            return ResponseEntity.badRequest().body(new CvResponse(false, "No CV content provided."));
        }

        // 4) Save
        student.setCvText(finalText);
        StudentEntity saved = repository.save(student);

        return ResponseEntity.ok(new CvResponse(true, saved.getCvText()));
    }

    // PUT /api/cv/update (JSON)
    @PutMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CvResponse> updateCv(@RequestBody CvUpdateRequest req) {
        StudentEntity student = service.getOrCreateCurrentStudent();

        String text = (req.getCvText() == null) ? "" : req.getCvText().trim();
        if (text.isEmpty()) {
            return ResponseEntity.badRequest().body(new CvResponse(false, "cvText cannot be empty."));
        }

        student.setCvText(text);
        StudentEntity saved = repository.save(student);

        return ResponseEntity.ok(new CvResponse(true, saved.getCvText()));
    }

    // DELETE /api/cv
    @DeleteMapping
    public ResponseEntity<CvResponse> deleteCv() {
        StudentEntity student = service.getOrCreateCurrentStudent();
        student.setCvText(null);
        repository.save(student);
        return ResponseEntity.ok(new CvResponse(false, null));
    }
}
