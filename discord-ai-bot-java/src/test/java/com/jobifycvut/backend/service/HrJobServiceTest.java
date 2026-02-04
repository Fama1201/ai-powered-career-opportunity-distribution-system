package com.jobifycvut.backend.service;

import com.jobifycvut.backend.dto.HrJobCreateRequest;
import com.jobifycvut.backend.model.Opportunity;
import com.jobifycvut.backend.repository.OpportunityRepository;
import com.jobifycvut.backend.security.SecurityUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.MockedStatic;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HrJobServiceTest {

    @Mock
    private OpportunityRepository opportunityRepository;

    @InjectMocks
    private HrJobService service;

    @Test
    void createJob_setsOwnerAndSaves() {
        HrJobCreateRequest req = new HrJobCreateRequest();
        req.setTitle("Intern");
        req.setApplicationDeadline(LocalDateTime.now());

        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::requireUserId).thenReturn(7L);
            when(opportunityRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            Opportunity out = service.createJob(req);

            assertEquals("7", out.getDiscordId());
            assertEquals("Intern", out.getTitle());
            assertNotNull(out.getOpportunityId());
        }
    }

    @Test
    void listMyJobs_openClosedArchived() {
        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::requireUserId).thenReturn(7L);

            when(opportunityRepository.findByDiscordIdAndApplicationDeadlineGreaterThanEqualOrderByIdDesc(
                    eq("7"), any(LocalDate.class))
            ).thenReturn(List.of(new Opportunity()));

            when(opportunityRepository.findByDiscordIdAndApplicationDeadlineLessThanOrderByIdDesc(
                    eq("7"), any(LocalDate.class))
            ).thenReturn(List.of(new Opportunity()));

            when(opportunityRepository.findByDiscordIdAndApplicationDeadlineIsNullOrderByIdDesc("7"))
                    .thenReturn(List.of(new Opportunity()));

            assertEquals(1, service.listMyJobs("OPEN").size());
            assertEquals(1, service.listMyJobs("CLOSED").size());
            assertEquals(1, service.listMyJobs("ARCHIVED").size());
        }
    }

    @Test
    void updateStatus_invalid_throws() {
        Opportunity job = new Opportunity();
        job.setId(1L);
        job.setDiscordId("7");
        when(opportunityRepository.findById(1L)).thenReturn(Optional.of(job));

        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::requireUserId).thenReturn(7L);

            ResponseStatusException ex = assertThrows(
                    ResponseStatusException.class,
                    () -> service.updateStatus(1L, "BAD")
            );

            assertEquals(400, ex.getStatusCode().value());
        }
    }

    @Test
    void updateStatus_archived_setsNullDeadline() {
        Opportunity job = new Opportunity();
        job.setId(1L);
        job.setDiscordId("7");
        job.setApplicationDeadline(LocalDate.now().plusDays(10));
        when(opportunityRepository.findById(1L)).thenReturn(Optional.of(job));

        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::requireUserId).thenReturn(7L);

            service.updateStatus(1L, "ARCHIVED");
            assertNull(job.getApplicationDeadline());
        }
    }

    @Test
    void updateMyJob_updatesFields() {
        Opportunity job = new Opportunity();
        job.setId(1L);
        job.setDiscordId("7");
        job.setTitle("Old");
        when(opportunityRepository.findById(1L)).thenReturn(Optional.of(job));

        HrJobCreateRequest req = new HrJobCreateRequest();
        req.setTitle("New");
        req.setApplicationDeadline(LocalDateTime.now());

        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::requireUserId).thenReturn(7L);

            Opportunity updated = service.updateMyJob(1L, req);
            assertEquals("New", updated.getTitle());
            assertNotNull(updated.getApplicationDeadline());
        }
    }
}
