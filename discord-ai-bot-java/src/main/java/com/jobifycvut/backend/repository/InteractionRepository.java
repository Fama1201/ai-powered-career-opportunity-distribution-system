package com.jobifycvut.backend.repository;

import com.jobifycvut.backend.model.InteractionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InteractionRepository extends JpaRepository<InteractionEntity, Long> {

    List<InteractionEntity> findTop50ByStudentIdAndActionOrderByCreatedAtDesc(Long studentId, String action);
    List<InteractionEntity> findTop50ByStudentIdAndActionInOrderByCreatedAtDesc(Long studentId, List<String> actions);

}
