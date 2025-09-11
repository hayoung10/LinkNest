package com.linknest.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
public class HealthController {
    @GetMapping("/api/health")
    public Map<String, Object> healthCheck() {
        return Map.of(
                "status", "OK",
                "time", Instant.now().toString()
        );
    }
}
