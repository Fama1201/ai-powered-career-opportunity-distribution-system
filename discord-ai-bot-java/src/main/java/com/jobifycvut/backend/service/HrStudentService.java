package com.jobifycvut.backend.service;

import com.jobifycvut.backend.dto.CvResponse;
import com.jobifycvut.backend.dto.StudentProfileResponse;
import com.jobifycvut.backend.model.StudentEntity;
import com.jobifycvut.backend.repository.StudentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class HrStudentService {

    private final StudentRepository studentRepository;

    public HrStudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public StudentProfileResponse getProfile(Long studentId) {
        StudentEntity student = findByIdOrThrow(studentId);
        return toResponse(student);
    }

    public CvResponse getCv(Long studentId) {
        StudentEntity student = findByIdOrThrow(studentId);

        String cvText = safeTrim(student.getCvText());
        boolean hasCv = !cvText.isEmpty();

        // If no CV, return null text (cleaner for frontend)
        return new CvResponse(hasCv, hasCv ? cvText : null);
    }

    /**
     * Search students for HR.
     * NOTE: we keep the signature (skills, major, keywords) so nothing breaks.
     */
    public List<StudentProfileResponse> search(String skills, String major, String keywords) {
        String skillsQ = blankToNull(skills);
        String majorQ = blankToNull(major);       // kept for signature compatibility
        String keywordsQ = blankToNull(keywords);

        List<StudentEntity> students = studentRepository.search(skillsQ, majorQ, keywordsQ);
        return students.stream().map(this::toResponse).toList();
    }

    private String blankToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }


    private StudentEntity findByIdOrThrow(Long studentId) {
        if (studentId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "studentId is required");
        }

        return studentRepository.findById(studentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
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

    private String normalizeQuery(String s) {
        // for search inputs: turn null/blank into empty string, trim whitespace
        return safeTrim(s);
    }

    private String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }
}
