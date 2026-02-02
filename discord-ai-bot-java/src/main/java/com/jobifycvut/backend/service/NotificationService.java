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

        return repository.findByStudentIdOrderByCreatedAtDesc(student.getId())
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

        NotificationEntity n = repository.findById(notificationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Notification not found"
                ));

        // If this notification is not for this student, forbid it
        if (n.getStudentId() == null || !n.getStudentId().equals(student.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Not allowed to modify this notification"
            );
        }

        n.setRead(true);
    }

    @Transactional
    public int markAllRead() {
        StudentEntity student = studentContextService.getOrCreateCurrentStudent();

        List<NotificationEntity> unread =
                repository.findByStudentIdAndReadFalseOrderByCreatedAtDesc(student.getId());

        for (NotificationEntity n : unread) {
            n.setRead(true);
        }

        return unread.size();
    }
}
