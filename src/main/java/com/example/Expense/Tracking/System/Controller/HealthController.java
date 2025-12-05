package com.example.Expense.Tracking.System.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/health")
    public String health() {
        return "OK";     // Used by Render health checks
    }

    @GetMapping("/")
    public String root() {
        return "RUNNING";  // Used by UptimeRobot to keep app awake
    }
}

