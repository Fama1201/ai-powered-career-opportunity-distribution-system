package com.jobifycvut.backend.service;

import com.jobifycvut.backend.dto.ForgotPasswordRequest;
import com.jobifycvut.backend.dto.HrLoginRequest;
import com.jobifycvut.backend.dto.HrRegisterRequest;
import com.jobifycvut.backend.model.HrUser;
import com.jobifycvut.backend.repository.HrUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HrService {

    private final HrUserRepository hrRepository;
    private final PasswordEncoder passwordEncoder;

    // Register: Hash password & Save
    public void register(HrRegisterRequest request) {
        if (hrRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }
        HrUser hr = new HrUser();
        hr.setEmail(request.getEmail());
        hr.setFullName(request.getFullName());
        hr.setCompanyName(request.getCompanyName());
        // 🔐 Security: Hash the password!
        hr.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        hr.setCreatedAt(LocalDateTime.now());
        hr.setHrTokens(UUID.randomUUID().toString()); // Initial token
        hrRepository.save(hr);
    }

    // Login: Verify Hash & Return User
    public HrUser login(HrLoginRequest request) {
        HrUser user = hrRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid Credentials");
        }
        return user;
    }

    // Logout: Log the action
    public void logout(String email) {
        System.out.println("🚪 HR User logged out: " + (email != null ? email : "Unknown"));
    }

    // Forgot Password: Generate Token & "Send" Email
    public void forgotPassword(ForgotPasswordRequest request) {
        HrUser user = hrRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String resetToken = UUID.randomUUID().toString();
        user.setHrTokens(resetToken); // Storing reset token
        hrRepository.save(user);

        // In a real app, you would use JavaMailSender here
        System.out.println("📧 [EMAIL SENT] to " + user.getEmail());
        System.out.println("🔗 Reset Link: http://localhost:3000/reset-password?token=" + resetToken);
    }
}