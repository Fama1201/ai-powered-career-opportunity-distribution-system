package com.jobifycvut.backend.service;

import com.jobifycvut.backend.dto.NotificationResponse;
import com.jobifycvut.backend.model.NotificationEntity;
import com.jobifycvut.backend.model.StudentEntity;
import com.jobifycvut.backend.repository.NotificationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository repository;
    private final StudentContextService studentContextService;

    public NotificationService(NotificationRepository repository,
                               StudentContextService studentContextService) {
        this.repository = repository;
        this.studentContextService = studentContextService;
    }

    public List<NotificationResponse> getMyNotifications() {
        StudentEntity student = studentContextService.getOrCreateCurrentStudent();
        // Get user_id from student's user relationship or userId field
        Long userId = student.getUserId();
        if (userId == null && student.getUser() != null) {
            userId = student.getUser().getId();
        }
        if (userId == null) {
            // Fallback: if no user relationship, try to use student ID (for backward compatibility)
            userId = student.getId();
        }

        return repository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(n -> new NotificationResponse(
                        n.getId(),
                        n.getMessage(),
                        n.getRead(),
                        n.getCreatedAt()
                ))
                .toList();
    }

    @Transactional
    public void markRead(Long notificationId) {
        StudentEntity student = studentContextService.getOrCreateCurrentStudent();
        Long userId = student.getUserId();
        if (userId == null && student.getUser() != null) {
            userId = student.getUser().getId();
        }
        if (userId == null) {
            userId = student.getId();
        }

        NotificationEntity n = repository.findById(notificationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Notification not found"
                ));

        // If this notification is not for this user, forbid it
        if (n.getUserId() == null || !n.getUserId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Not allowed to modify this notification"
            );
        }

        n.setRead(true);
        repository.save(n);
    }

    @Transactional
    public int markAllRead() {
        StudentEntity student = studentContextService.getOrCreateCurrentStudent();
        Long userId = student.getUserId();
        if (userId == null && student.getUser() != null) {
            userId = student.getUser().getId();
        }
        if (userId == null) {
            userId = student.getId();
        }

        List<NotificationEntity> unread =
                repository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId);

        for (NotificationEntity n : unread) {
            n.setRead(true);
        }
        repository.saveAll(unread);

        return unread.size();
    }
}
