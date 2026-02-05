package com.jobifycvut.backend.repository;

import com.jobifycvut.backend.model.LearningAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LearningAssignmentRepository extends JpaRepository<LearningAssignment, Long> {
    List<LearningAssignment> findByStudentIdOrderByCreatedAtDesc(Long studentId);
    List<LearningAssignment> findByStudentIdAndCategoryOrderByCreatedAtDesc(Long studentId, String category);
}
