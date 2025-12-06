package com.jobifycvut.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobifycvut.backend.dto.ForgotPasswordRequest;
import com.jobifycvut.backend.dto.HrLoginRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// ✅ FIXED: Explicitly point to the main application class to load the full context
@SpringBootTest(classes = com.jobifycvut.backend.JobifyBackendApplication.class)
@AutoConfigureMockMvc
public class HrAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper; // Converts Objects to JSON

    @Test
    public void testHrLogin_Success() throws Exception {
        // Prepare the login request
        HrLoginRequest loginRequest = new HrLoginRequest();

        // ✅ Use the REAL user credentials from your database screenshot
        loginRequest.setEmail("alice.hr@google.com");
        loginRequest.setPassword("hash123");

        // Perform POST request
        mockMvc.perform(post("/api/hr/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk()) // Expect 200 OK
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    public void testHrLogout_Success() throws Exception {
        mockMvc.perform(post("/api/hr/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out successfully"));
    }

    @Test
    public void testForgotPassword_Success() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("forgot@hr.com");

        mockMvc.perform(post("/api/hr/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("If an account exists, a reset email has been sent."));
    }
}