package com.jobifycvut.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // API + JWT
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Disable default auth mechanisms (prevents weird 403/redirect behavior)
                .formLogin(f -> f.disable())
                .httpBasic(b -> b.disable())
                .logout(l -> l.disable())

                // IMPORTANT: make missing/invalid token return 401 instead of 403
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )

                .authorizeHttpRequests(auth -> auth
                        // CORS preflight must always pass
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // allow Spring error endpoint
                        .requestMatchers("/error").permitAll()

                        // PUBLIC: auth endpoints
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/hr/auth/**").permitAll()

                        // Public browsing
                        .requestMatchers(HttpMethod.GET, "/api/jobs/**").permitAll()

                        // Student actions on jobs
                        .requestMatchers(HttpMethod.POST, "/api/jobs/*/apply").hasRole("STUDENT")
                        .requestMatchers(HttpMethod.POST, "/api/jobs/*/save").hasRole("STUDENT")

                        // HR protected routes
                        .requestMatchers("/api/hr/**").hasRole("HR")

                        // Student protected routes
                        .requestMatchers("/api/dashboard/**").hasRole("STUDENT")
                        .requestMatchers("/api/applications/**").hasRole("STUDENT")
                        .requestMatchers("/api/ai/**").hasRole("STUDENT")
                        .requestMatchers("/api/profile/**").hasRole("STUDENT")
                        .requestMatchers("/api/notifications/**").hasRole("STUDENT")
                        .requestMatchers("/api/cv/**").hasRole("STUDENT")

                        // everything else under /api needs auth (safe fallback)
                        .requestMatchers("/api/**").authenticated()

                        .anyRequest().permitAll()
                )

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
