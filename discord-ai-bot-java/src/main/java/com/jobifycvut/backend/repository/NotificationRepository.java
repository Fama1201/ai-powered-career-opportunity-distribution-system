package com.jobifycvut.backend.repository;

import com.jobifycvut.backend.model.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    List<NotificationEntity> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    List<NotificationEntity> findByStudentIdAndReadFalseOrderByCreatedAtDesc(Long studentId);
}
