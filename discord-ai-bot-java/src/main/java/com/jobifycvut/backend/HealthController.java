package com.jobifycvut.backend; // O .controller si creaste subpaquete

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/api/status")
    public String checkStatus() {
        return "✅ Jobify Backend está online. Hola Cisco!";
    }
}