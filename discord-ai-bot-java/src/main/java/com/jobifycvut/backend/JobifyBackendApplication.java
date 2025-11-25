package com.jobifycvut.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
// 1. Force Spring to scan 'com.jobifycvut.backend' AND 'storage' for Components (@Component, @Service, @Controller)
@ComponentScan(basePackages = {
        "com.jobifycvut.backend",
        "storage"
})
// 2. Force Spring to scan 'storage' for Database Repositories (DAOs)
@EnableJpaRepositories(basePackages = {
        "storage",
        "com.jobifycvut.backend.repository"
})
// 3. Force Spring to scan for Database Entities (tables)
@EntityScan(basePackages = {
        "com.jobifycvut.backend.model",
        "storage"
})
public class JobifyBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobifyBackendApplication.class, args);
        System.out.println("🚀 Backend Web iniciado en el puerto 8081");
    }
}