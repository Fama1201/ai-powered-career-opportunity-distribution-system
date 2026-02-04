package com.jobifycvut.backend.service;

import com.jobifycvut.backend.dto.ApplicationResponse;
import com.jobifycvut.backend.model.JobApplicationEntity;
import com.jobifycvut.backend.repository.JobApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private JobApplicationRepository repo;

    @InjectMocks
    private ApplicationService service;

    private JobApplicationEntity app;

    @BeforeEach
    void setUp() {
        app = new JobApplicationEntity();
        app.setId(1L);
        app.setUserId(2L);
        app.setOpportunityId(3L);
        app.setStatus("APPLIED");
    }

    @Test
    void getApplications_byUserAndOpportunity() {
        when(repo.findByUserIdAndOpportunityId(2L, 3L)).thenReturn(List.of(app));

        List<ApplicationResponse> out = service.getApplications(2L, 3L, null);

        assertEquals(1, out.size());
        assertEquals(2L, out.get(0).userId);
    }

    @Test
    void getApplications_byStatus() {
        when(repo.findByStatusIgnoreCase("applied")).thenReturn(List.of(app));

        List<ApplicationResponse> out = service.getApplications(null, null, "applied");

        assertEquals(1, out.size());
    }

    @Test
    void getById_missing_throws() {
        when(repo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.getById(99L));
    }

    @Test
    void withdraw_setsStatusWhenNotWithdrawn() {
        app.setStatus("APPLIED");
        when(repo.findById(1L)).thenReturn(Optional.of(app));

        ApplicationResponse out = service.withdraw(1L);

        assertEquals("WITHDRAWN", out.status);
    }

    @Test
    void withdraw_keepsStatusWhenAlreadyWithdrawn() {
        app.setStatus("WITHDRAWN");
        when(repo.findById(1L)).thenReturn(Optional.of(app));

        ApplicationResponse out = service.withdraw(1L);

        assertEquals("WITHDRAWN", out.status);
    }
}
