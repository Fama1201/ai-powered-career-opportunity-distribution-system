package com.jobifycvut.backend.controller;

import com.jobifycvut.backend.dto.StudentProfileResponse;
import com.jobifycvut.backend.dto.StudentProfileUpdateRequest;
import com.jobifycvut.backend.model.StudentEntity;
import com.jobifycvut.backend.model.User;
import com.jobifycvut.backend.repository.StudentRepository;
import com.jobifycvut.backend.repository.UserRepository;
import com.jobifycvut.backend.security.SecurityUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ProfileController {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

    public ProfileController(StudentRepository studentRepository, UserRepository userRepository) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
    }

    // -------------------- GET --------------------

    @GetMapping("/profile")
    public ResponseEntity<StudentProfileResponse> getMyProfile() {
        StudentEntity student = getOrCreateStudentForCurrentUser();
        return ResponseEntity.ok(toResponse(student));
    }

    @GetMapping("/settings/profile")
    public ResponseEntity<StudentProfileResponse> getMyProfileSettings() {
        StudentEntity student = getOrCreateStudentForCurrentUser();
        return ResponseEntity.ok(toResponse(student));
    }

    // -------------------- PUT --------------------

    @PutMapping("/profile")
    public ResponseEntity<StudentProfileResponse> updateMyProfile(@RequestBody StudentProfileUpdateRequest req) {
        StudentEntity student = getOrCreateStudentForCurrentUser();

        if (req.getName() != null) student.setName(req.getName());
        if (req.getSkills() != null) student.setSkills(req.getSkills());
        if (req.getCareerInterest() != null) student.setCareerInterest(req.getCareerInterest());

        StudentEntity saved = studentRepository.save(student);
        return ResponseEntity.ok(toResponse(saved));
    }

    @PutMapping("/settings/profile")
    public ResponseEntity<StudentProfileResponse> updateMyProfileSettings(@RequestBody StudentProfileUpdateRequest req) {
        // same behavior for now
        return updateMyProfile(req);
    }

    // -------------------- helpers --------------------

    /**
     * Ensures a StudentEntity exists for the logged-in user.
     *
     * Why needed:
     * Your auth register creates a User, but the student row might not exist yet.
     * This method creates it on-demand the first time profile/dashboard is accessed.
     */
    private StudentEntity getOrCreateStudentForCurrentUser() {
        Long userId = SecurityUtil.requireUserId();
        String emailFromJwt = SecurityUtil.getPrincipal() != null ? SecurityUtil.getPrincipal().getEmail() : null;

        // 1) If student ID matches userId (common in your setup)
        var byId = studentRepository.findById(userId);
        if (byId.isPresent()) return byId.get();

        // 2) Fallback to find by email
        if (emailFromJwt != null && !emailFromJwt.isBlank()) {
            var byEmail = studentRepository.findByEmail(emailFromJwt);
            if (byEmail.isPresent()) return byEmail.get();
        }

        // 3) Create new student row using User table info
        User user = userRepository.findById(userId).orElse(null);

        StudentEntity student = new StudentEntity();
        if (user != null) {
            student.setEmail(user.getEmail());
            student.setName(user.getFullName());
        } else {
            // last fallback
            student.setEmail(emailFromJwt);
            student.setName("Student");
        }

        // skills/careerInterest can start null or empty
        return studentRepository.save(student);
    }

    private StudentProfileResponse toResponse(StudentEntity s) {
        return new StudentProfileResponse(
                s.getId(),
                s.getName(),
                s.getEmail(),
                s.getSkills(),
                s.getCareerInterest()
        );
    }
}
