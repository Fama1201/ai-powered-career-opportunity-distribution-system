package com.jobifycvut.backend.service;

import com.jobifycvut.backend.dto.HrApplicationDetailResponse;
import com.jobifycvut.backend.dto.HrApplicationListItemResponse;
import com.jobifycvut.backend.model.JobApplicationEntity;
import com.jobifycvut.backend.model.Opportunity;
import com.jobifycvut.backend.model.StudentEntity;
import com.jobifycvut.backend.model.User;
import com.jobifycvut.backend.repository.JobApplicationRepository;
import com.jobifycvut.backend.repository.OpportunityRepository;
import com.jobifycvut.backend.repository.StudentRepository;
import com.jobifycvut.backend.repository.UserRepository;
import com.jobifycvut.backend.security.SecurityUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HrCandidatePipelineServiceTest {

    @Mock
    private JobApplicationRepository applicationRepository;
    @Mock
    private OpportunityRepository opportunityRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private HrCandidatePipelineService service;

    @Test
    void listApplicantsForJob_returnsList() {
        Opportunity job = new Opportunity();
        job.setId(10L);
        job.setDiscordId("7");
        when(opportunityRepository.findById(10L)).thenReturn(Optional.of(job));

        JobApplicationEntity app = new JobApplicationEntity();
        app.setId(1L);
        app.setUserId(5L);
        app.setOpportunityId(10L);
        app.setStatus("APPLIED");
        app.setAppliedAt(OffsetDateTime.now());

        when(applicationRepository.findByOpportunityId(10L)).thenReturn(List.of(app));
        User user = new User();
        user.setEmail("u@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));

        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::requireUserId).thenReturn(7L);
            List<HrApplicationListItemResponse> out = service.listApplicantsForJob(10L);
            assertEquals(1, out.size());
            assertEquals("u@example.com", out.get(0).getStudentEmail());
        }
    }

    @Test
    void getApplicationDetail_includesStudentData() {
        Opportunity job = new Opportunity();
        job.setId(10L);
        job.setDiscordId("7");
        when(opportunityRepository.findById(10L)).thenReturn(Optional.of(job));

        JobApplicationEntity app = new JobApplicationEntity();
        app.setId(1L);
        app.setUserId(5L);
        app.setOpportunityId(10L);
        app.setStatus("APPLIED");
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(app));

        User user = new User();
        user.setEmail("u@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));

        StudentEntity student = new StudentEntity();
        student.setEmail("u@example.com");
        student.setSkills("Java");
        when(studentRepository.findByEmail("u@example.com")).thenReturn(Optional.of(student));

        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::requireUserId).thenReturn(7L);
            HrApplicationDetailResponse out = service.getApplicationDetail(1L);
            assertEquals("Java", out.getStudentSkills());
        }
    }

    @Test
    void updateApplicationStatus_invalidStatus_throws() {
        JobApplicationEntity app = new JobApplicationEntity();
        app.setId(1L);
        app.setUserId(5L);
        app.setOpportunityId(10L);

        Opportunity job = new Opportunity();
        job.setId(10L);
        job.setDiscordId("7");

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(app));
        when(opportunityRepository.findById(10L)).thenReturn(Optional.of(job));

        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::requireUserId).thenReturn(7L);
            ResponseStatusException ex = assertThrows(
                    ResponseStatusException.class,
                    () -> service.updateApplicationStatus(1L, "bad")
            );
            assertEquals(400, ex.getStatusCode().value());
        }
    }

    @Test
    void addOrUpdateNote_appendsNote() {
        JobApplicationEntity app = new JobApplicationEntity();
        app.setId(1L);
        app.setUserId(5L);
        app.setOpportunityId(10L);
        app.setNotes("Old");

        Opportunity job = new Opportunity();
        job.setId(10L);
        job.setDiscordId("7");

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(app));
        when(opportunityRepository.findById(10L)).thenReturn(Optional.of(job));

        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::requireUserId).thenReturn(7L);
            service.addOrUpdateNote(1L, "New");
            assertTrue(app.getNotes().contains("Old"));
            assertTrue(app.getNotes().contains("New"));
        }
    }
}
