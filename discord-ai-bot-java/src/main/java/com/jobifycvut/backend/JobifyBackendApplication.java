package com.jobifycvut.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JobifyBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobifyBackendApplication.class, args);
        System.out.println("🚀 Backend Web iniciado en el puerto 8080");
    }
}
