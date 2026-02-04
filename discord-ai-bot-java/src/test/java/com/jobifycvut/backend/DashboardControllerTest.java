package com.jobifycvut.backend;

import com.jobifycvut.backend.model.StudentEntity;
import com.jobifycvut.backend.repository.StudentRepository;
import com.jobifycvut.backend.security.AuthPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = com.jobifycvut.backend.JobifyBackendApplication.class)
@AutoConfigureMockMvc
public class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudentRepository studentRepository;

    private static final String TEST_EMAIL = "student_test@example.com";
    private UsernamePasswordAuthenticationToken authToken;

    @BeforeEach
    public void setup() {
        studentRepository.findByEmail(TEST_EMAIL).ifPresent(studentRepository::delete);

        // Create a dummy student for testing
        StudentEntity student = new StudentEntity();
        student.setEmail(TEST_EMAIL);
        student.setName("Test Student");
        student.setCareerInterest("Java"); // Should match jobs with "Java" in title
        studentRepository.save(student);

        AuthPrincipal principal = new AuthPrincipal(1L, TEST_EMAIL, "STUDENT");
        authToken = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_STUDENT"))
        );
    }

    @AfterEach
    public void tearDown() {
        authToken = null;
    }

    @Test
    public void testGetOverview() throws Exception {
        mockMvc.perform(get("/api/dashboard/overview").with(authentication(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentName").value("Test Student"));
    }

    @Test
    public void testGetNewJobs() throws Exception {
        mockMvc.perform(get("/api/dashboard/jobs/new").with(authentication(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    public void testGetProgress() throws Exception {
        mockMvc.perform(get("/api/dashboard/progress").with(authentication(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completionPercentage").exists());
    }
}
