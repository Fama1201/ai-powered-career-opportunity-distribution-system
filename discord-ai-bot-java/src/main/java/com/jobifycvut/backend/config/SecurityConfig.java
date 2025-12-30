package com.jobifycvut.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Global Security Configuration for the API.
 * Defines password encoding standards and public access rules.
 */
@Configuration
@EnableWebSecurity // Enables Spring Security's web capabilities
public class SecurityConfig {

    /**
     * Defines the PasswordEncoder bean used across the application (e.g., in HrService)
     * for securely hashing and verifying passwords. BCrypt is the industry standard.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures the Security Filter Chain, defining authorization rules for HTTP requests.
     * * @param http The HttpSecurity object for configuring web security.
     * @return The configured SecurityFilterChain.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // 1. Disable Cross-Site Request Forgery (CSRF) protection.
                // This is standard for stateless REST APIs where session management is handled by tokens (JWT).
                .csrf(AbstractHttpConfigurer::disable)

                // 2. Configure request authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Permit public access to all authentication endpoints (/register, /login, etc.)
                        .requestMatchers("/api/hr/auth/**").permitAll()

                        // --- ADD THIS LINE TO ALLOW DASHBOARD TESTING ---
                        .requestMatchers("/api/dashboard/**").permitAll()

                        // Require authentication (a valid token/session) for all other API requests
                        .anyRequest().authenticated()
                )

                // 3. Disable default login mechanisms (we use custom JSON/token logic)
                // Disables HTTP Basic authentication (browser pop-up)
                .httpBasic(AbstractHttpConfigurer::disable)
                // Disables Spring's default form-based login
                .formLogin(AbstractHttpConfigurer::disable)

                .build();
    }
}