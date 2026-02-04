package com.jobifycvut.backend.service;

import com.jobifycvut.backend.dto.*;
import com.jobifycvut.backend.exception.AuthException;
import com.jobifycvut.backend.model.User;
import com.jobifycvut.backend.model.UserRole;
import com.jobifycvut.backend.repository.UserRepository;
import com.jobifycvut.backend.util.JwtUtil;
import com.jobifycvut.backend.util.PasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthenticationService service;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRole(UserRole.STUDENT);
        user.setPassword(PasswordHasher.hashPassword("pass"));
    }

    @Test
    void register_whenEmailExists_throws() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("a@b.com");
        req.setPassword("pass");
        req.setFirstName("A");
        req.setLastName("B");
        when(userRepository.existsByEmail("a@b.com")).thenReturn(true);
        assertThrows(AuthException.class, () -> service.register(req));
    }

    @Test
    void login_success_returnsTokens() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generateAccessToken(user)).thenReturn("access");
        when(jwtUtil.generateRefreshToken(user)).thenReturn("refresh");

        LoginRequest req = new LoginRequest("user@example.com", "pass");
        AuthResponse response = service.login(req);

        assertEquals("access", response.getAccessToken());
        assertEquals("refresh", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        verify(userRepository).save(user);
    }

    @Test
    void login_invalidPassword_throws() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        LoginRequest req = new LoginRequest("user@example.com", "wrong");
        assertThrows(AuthException.class, () -> service.login(req));
    }

    @Test
    void refreshToken_valid() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        claims.put("userId", 1L);
        when(jwtUtil.validateToken("token")).thenReturn(claims);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(jwtUtil.generateAccessToken(user)).thenReturn("newAccess");

        RefreshTokenRequest req = new RefreshTokenRequest("token");
        AuthResponse response = service.refreshToken(req);

        assertEquals("newAccess", response.getAccessToken());
        assertEquals("token", response.getRefreshToken());
    }

    @Test
    void verifyToken_invalid() {
        when(jwtUtil.validateToken("bad")).thenReturn(null);
        TokenValidationResponse resp = service.verifyToken("bad");
        assertFalse(resp.isValid());
    }

    @Test
    void resetPassword_expiredToken_throws() {
        user.setPasswordResetToken("t");
        user.setPasswordResetTokenExpiry(Instant.now().minusSeconds(60));
        when(userRepository.findByPasswordResetToken("t")).thenReturn(Optional.of(user));

        ResetPasswordRequest req = new ResetPasswordRequest("t", "newpass");
        assertThrows(AuthException.class, () -> service.resetPassword(req));
    }
}
