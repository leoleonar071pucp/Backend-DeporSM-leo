package com.example.deporsm.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;
import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthCheckController {

    @Value("${app.cors.allowed-origins:http://localhost:3000}")
    private String allowedOriginsString;

    @GetMapping("")
    public ResponseEntity<Map<String, Object>> checkHealth(
            @RequestHeader(value = "Origin", required = false) String origin,
            HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", System.currentTimeMillis());
        response.put("service", "DeporSM-API");

        // Información sobre el perfil activo
        String activeProfile = System.getProperty("spring.profiles.active");
        response.put("activeProfile", activeProfile != null ? activeProfile : "no definido");

        // Información sobre CORS
        response.put("allowedOrigins", allowedOriginsString);
        response.put("requestOrigin", origin != null ? origin : "no definido");

        // Información sobre cookies
        response.put("cookieConfig", Map.of(
            "sameSite", activeProfile != null && activeProfile.contains("prod") ? "None" : "Lax",
            "secure", activeProfile != null && activeProfile.contains("prod")
        ));

        return ResponseEntity.ok()
            .header("Cache-Control", "no-cache, no-store, must-revalidate")
            .header("Pragma", "no-cache")
            .body(response);
    }
}
