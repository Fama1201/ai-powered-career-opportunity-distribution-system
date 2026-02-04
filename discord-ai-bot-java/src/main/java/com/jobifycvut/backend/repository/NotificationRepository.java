package com.jobifycvut.backend.repository;

import com.jobifycvut.backend.model.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    List<NotificationEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<NotificationEntity> findByUserIdAndReadFalseOrderByCreatedAtDesc(Long userId);
    
    // Backward compatibility - deprecated
    @Deprecated
    default List<NotificationEntity> findByStudentIdOrderByCreatedAtDesc(Long studentId) {
        return findByUserIdOrderByCreatedAtDesc(studentId);
    }
    
    @Deprecated
    default List<NotificationEntity> findByStudentIdAndReadFalseOrderByCreatedAtDesc(Long studentId) {
        return findByUserIdAndReadFalseOrderByCreatedAtDesc(studentId);
    }
}
