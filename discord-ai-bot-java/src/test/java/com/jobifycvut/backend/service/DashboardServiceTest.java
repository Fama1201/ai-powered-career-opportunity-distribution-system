package com.jobifycvut.backend.service;

import com.jobifycvut.backend.dto.DashboardOverviewResponse;
import com.jobifycvut.backend.dto.ProgressResponse;
import com.jobifycvut.backend.model.Opportunity;
import com.jobifycvut.backend.model.StudentEntity;
import com.jobifycvut.backend.repository.JobApplicationRepository;
import com.jobifycvut.backend.repository.OpportunityRepository;
import com.jobifycvut.backend.repository.SavedJobRepository;
import com.jobifycvut.backend.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.jobifycvut.backend.dto.OpportunityListResponse;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private StudentRepository studentRepository;
    @Mock
    private OpportunityRepository opportunityRepository;
    @Mock
    private JobApplicationRepository applicationRepository;
    @Mock
    private SavedJobRepository savedJobRepository;
    @Mock
    private MatchService matchService;

    @InjectMocks
    private DashboardService service;

    private StudentEntity student;

    @BeforeEach
    void setUp() {
        student = new StudentEntity();
        student.setId(1L);
        student.setEmail("student@example.com");
        student.setName("Student One");
        student.setCareerInterest("Java");

        lenient().when(matchService.findMatches(any(StudentEntity.class), anyInt()))
                .thenReturn(List.of());
    }

    @Test
    void getOverview_returnsCountsAndMatches() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(applicationRepository.countByUserId(1L)).thenReturn(2L);
        when(savedJobRepository.countByUserId(1L)).thenReturn(3L);

        Opportunity opp = new Opportunity();
        opp.setId(100L);
        opp.setTitle("Java Intern");
        opp.setCompany("TestCo");
        
        when(matchService.findMatches(any(StudentEntity.class), anyInt()))
                .thenReturn(List.of(
                        new com.jobifycvut.backend.dto.OpportunityListResponse(
                                opp.getId(),
                                opp.getTitle(),
                                opp.getCompany(),
                                null,
                                null,
                                null,
                                null
                        )
                ));

        DashboardOverviewResponse response = service.getOverview(1L, "student@example.com");

        assertEquals("Student One", response.getStudentName());
        assertEquals(2, response.getTotalApplications());
        assertEquals(3, response.getSavedJobsCount());
        assertEquals(1, response.getRecentMatches().size());
    }

    @Test
    void getProgress_whenProfileIncomplete() {
        StudentEntity s = new StudentEntity();
        s.setId(1L);
        s.setEmail("student@example.com");
        when(studentRepository.findById(1L)).thenReturn(Optional.of(s));

        ProgressResponse response = service.getProgress(1L, "student@example.com");

        assertEquals(25, response.getCompletionPercentage());
    }

    @Test
    void getProgress_whenNoStudent() {
        when(studentRepository.findById(1L)).thenReturn(Optional.empty());
        when(studentRepository.findByEmail("student@example.com")).thenReturn(Optional.empty());

        ProgressResponse response = service.getProgress(1L, "student@example.com");

        assertEquals(0, response.getCompletionPercentage());
        assertEquals("Profile not created", response.getMessage());
    }
}
