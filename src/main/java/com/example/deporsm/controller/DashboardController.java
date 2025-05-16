package com.example.deporsm.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class DashboardController {

    @GetMapping("/dashboard")
    public Map<String, Object> getDashboardData() {
        Map<String, Object> dashboardData = new HashMap<>();

        // Aquí pondrás los datos reales que quieras mostrar en tu dashboard
        dashboardData.put("totalReservas", 248);
        dashboardData.put("reservasActivas", 42);
        dashboardData.put("totalInstalaciones", 5);
        dashboardData.put("totalObservaciones", 3);

        return dashboardData;
    }
}
