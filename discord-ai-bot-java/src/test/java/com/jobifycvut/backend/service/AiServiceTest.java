package com.jobifycvut.backend.service;

import bot.api.OpportunityClient;
import com.jobifycvut.backend.dto.ChatResponse;
import com.jobifycvut.backend.dto.CodeReviewRequest;
import com.jobifycvut.backend.dto.CodeReviewResponse;
import com.jobifycvut.backend.model.InteractionEntity;
import com.jobifycvut.backend.model.StudentEntity;
import com.jobifycvut.backend.repository.InteractionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiServiceTest {

    @Mock
    private InteractionRepository interactionRepository;

    @Mock
    private StudentContextService studentContextService;

    @Mock
    private JobSearchClient jobSearchClient;

    @Mock
    private OpenAiClient openAiClient;

    @InjectMocks
    private AiService aiService;

    @Captor
    private ArgumentCaptor<InteractionEntity> interactionCaptor;

    private StudentEntity student;

    @BeforeEach
    void setUp() {
        student = new StudentEntity();
        student.setId(10L);
        student.setName("Jane Doe");
        student.setEmail("jane@example.com");
        student.setSkills("Java Spring SQL");
        student.setCareerInterest("Backend");
        student.setCvText("CV content");
    }

    @Test
    void chat_whenCvMissing_returnsBadRequest() {
        student.setCvText("   ");
        when(studentContextService.getOrCreateCurrentStudent()).thenReturn(student);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> aiService.chat(1L, "hi")
        );

        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void chat_assignmentsRequested_returnsThreeAssignments() throws Exception {
        when(studentContextService.getOrCreateCurrentStudent()).thenReturn(student);

        Set<OpportunityClient.Opportunity> jobs = new HashSet<>();
        for (int i = 0; i < 12; i++) {
            jobs.add(job("ID" + i, "Job " + i));
        }
        when(jobSearchClient.searchMultipleKeywords(anyString()))
                .thenReturn(jobs);

        String aiJson = """
                {
                  "cvScore": 70,
                  "summary": "Summary",
                  "missingSkills": [],
                  "recommendations": [],
                  "assignments": [],
                  "topMatches": [
                    {"id":"ID1","title":"Job 1","company":"Co","url":"u","score":80,"matchReason":"r"}
                  ]
                }
                """;

        when(openAiClient.chatJson(anyString(), anyString(), anyList(), any()))
                .thenReturn(aiJson);

        ChatResponse response = aiService.chat(1L, "Give me 3 assignments to improve my backend skills.");

        assertNotNull(response.getAssignments());
        assertEquals(3, response.getAssignments().size());
    }

    @Test
    void chat_fillsTopMatchesToTenWhenJobsAvailable() throws Exception {
        when(studentContextService.getOrCreateCurrentStudent()).thenReturn(student);

        Set<OpportunityClient.Opportunity> jobs = new HashSet<>();
        for (int i = 0; i < 15; i++) {
            jobs.add(job("ID" + i, "Backend Job " + i));
        }
        when(jobSearchClient.searchMultipleKeywords(anyString()))
                .thenReturn(jobs);

        String aiJson = """
                {
                  "cvScore": 70,
                  "summary": "Summary",
                  "missingSkills": [],
                  "recommendations": [],
                  "assignments": [],
                  "topMatches": [
                    {"id":"ID1","title":"Job 1","company":"Co","url":"u","score":80,"matchReason":"r"}
                  ]
                }
                """;

        when(openAiClient.chatJson(anyString(), anyString(), anyList(), any()))
                .thenReturn(aiJson);

        ChatResponse response = aiService.chat(1L, "Focus on backend internships");

        assertNotNull(response.getTopMatches());
        assertEquals(10, response.getTopMatches().size());
    }

    @Test
    void codeReview_returnsParsedResponseAndSavesInteraction() throws Exception {
        when(studentContextService.getOrCreateCurrentStudent()).thenReturn(student);

        String aiJson = """
                {
                  "summary": "Looks good overall.",
                  "issues": [
                    {"severity":"LOW","title":"Naming","description":"Use clearer names."}
                  ],
                  "suggestions": ["Add tests"]
                }
                """;

        when(openAiClient.chatJson(anyString(), anyString(), anyList(), any()))
                .thenReturn(aiJson);

        CodeReviewRequest req = new CodeReviewRequest("java", "class A {}");
        CodeReviewResponse response = aiService.codeReview(1L, req);

        assertEquals("Looks good overall.", response.getSummary());
        assertEquals(1, response.getIssues().size());
        assertEquals("LOW", response.getIssues().get(0).getSeverity());
        assertEquals(1, response.getSuggestions().size());

        verify(interactionRepository).save(interactionCaptor.capture());
        assertEquals("AI_CODE_REVIEW", interactionCaptor.getValue().getAction());
        assertEquals(10L, interactionCaptor.getValue().getStudentId());
    }

    @Test
    void codeReview_emptyCode_returnsBadRequest() {
        CodeReviewRequest req = new CodeReviewRequest("java", "   ");
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> aiService.codeReview(1L, req)
        );
        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void chat_propagatesOpenAiFailure() throws Exception {
        when(studentContextService.getOrCreateCurrentStudent()).thenReturn(student);

        Set<OpportunityClient.Opportunity> jobs = new HashSet<>();
        jobs.add(job("ID1", "Backend Job"));
        when(jobSearchClient.searchMultipleKeywords(anyString()))
                .thenReturn(jobs);

        when(openAiClient.chatJson(anyString(), anyString(), anyList(), any()))
                .thenThrow(new IOException("boom"));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> aiService.chat(1L, "Focus on backend")
        );

        assertEquals(502, ex.getStatusCode().value());
    }

    private OpportunityClient.Opportunity job(String id, String title) {
        OpportunityClient.Opportunity job = new OpportunityClient.Opportunity();
        job.id = id;
        job.title = title;
        job.company = "Co";
        job.description = "Backend Java Spring";
        job.url = "https://example.com";
        return job;
    }
}
