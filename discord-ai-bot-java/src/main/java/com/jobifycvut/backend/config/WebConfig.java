package com.jobifycvut.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    // Spring Boot automatically serves static files from classpath:/static/
    // No additional configuration needed
}

