package com.jobifycvut.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobifycvut.backend.dto.ForgotPasswordRequest;
import com.jobifycvut.backend.dto.HrLoginRequest;
import com.jobifycvut.backend.dto.HrRegisterRequest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = com.jobifycvut.backend.JobifyBackendApplication.class)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HrAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // We generate a random email so the test works every time (avoiding "Email already exists" error)
    private static final String TEST_EMAIL = "hr_test_" + UUID.randomUUID() + "@example.com";
    private static final String TEST_PASSWORD = "StrongPassword123!";

    @Test
    @Order(1)
    public void test1_Register_Success() throws Exception {
        HrRegisterRequest registerRequest = new HrRegisterRequest();
        registerRequest.setEmail(TEST_EMAIL);
        registerRequest.setPassword(TEST_PASSWORD);
        registerRequest.setFullName("Test HR User");
        registerRequest.setCompanyName("Test Corp");

        mockMvc.perform(post("/api/hr/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("HR User registered successfully"));
    }

    @Test
    @Order(2)
    public void test2_Login_Success() throws Exception {
        HrLoginRequest loginRequest = new HrLoginRequest();
        loginRequest.setEmail(TEST_EMAIL);
        loginRequest.setPassword(TEST_PASSWORD);

        mockMvc.perform(post("/api/hr/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.accessToken").exists()) // Check if token is generated
                .andExpect(jsonPath("$.email").value(TEST_EMAIL));
    }

    @Test
    @Order(3)
    public void test3_ForgotPassword_Success() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail(TEST_EMAIL);

        mockMvc.perform(post("/api/hr/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("If an account exists, a reset email has been sent."));
    }

    @Test
    @Order(4)
    public void test4_Logout_Success() throws Exception {
        // Logout is simple, just checking the endpoint response
        Map<String, String> logoutBody = Map.of("email", TEST_EMAIL);

        mockMvc.perform(post("/api/hr/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out successfully"));
    }
}