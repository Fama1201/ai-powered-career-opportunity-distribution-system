package com.jobifycvut.backend.repository;

import com.jobifycvut.backend.model.AssignmentSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, Long> {
    List<AssignmentSubmission> findByAssignmentIdOrderByCreatedAtDesc(Long assignmentId);
}
