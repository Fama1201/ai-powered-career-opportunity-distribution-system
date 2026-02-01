package com.jobifycvut.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobifycvut.backend.model.StudentEntity;
import com.jobifycvut.backend.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = com.jobifycvut.backend.JobifyBackendApplication.class)
@AutoConfigureMockMvc
public class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudentRepository studentRepository;

    private static final String TEST_DISCORD_ID = "999888777";

    @BeforeEach
    public void setup() {
        // Create a dummy student for testing
        StudentEntity student = new StudentEntity();
        student.setDiscordId(TEST_DISCORD_ID);
        student.setName("Test Student");
        student.setCareerInterest("Java"); // Should match jobs with "Java" in title
        studentRepository.save(student);
    }

    @Test
    public void testGetOverview() throws Exception {
        mockMvc.perform(get("/api/dashboard/overview").param("discordId", TEST_DISCORD_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentName").value("Test Student"));
    }

    @Test
    public void testGetNewJobs() throws Exception {
        mockMvc.perform(get("/api/dashboard/jobs/new"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    public void testGetProgress() throws Exception {
        mockMvc.perform(get("/api/dashboard/progress").param("discordId", TEST_DISCORD_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completionPercentage").exists());
    }
}