package com.example.deporsm.controller;

import com.example.deporsm.model.LogActividad;
import com.example.deporsm.service.LogActividadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
@CrossOrigin(
  origins = {
    "https://deporsm-apiwith-1035693188565.us-central1.run.app",
    "https://frontend-depor-sm-leo.vercel.app",
    "http://localhost:3000"
  },
  allowCredentials = "true"
)
public class LogActividadController {

    @Autowired
    private LogActividadService logActividadService;

    @GetMapping("/actividad")
    public ResponseEntity<List<LogActividad>> obtenerActividad(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
                
        List<LogActividad> logs = logActividadService.findByFilters(role, action, status, startDate, endDate);
        return ResponseEntity.ok(logs);
    }
    
    @GetMapping("/actividad/recientes")
    public ResponseEntity<List<LogActividad>> obtenerActividadReciente() {
        List<LogActividad> recentLogs = logActividadService.findMostRecent();
        return ResponseEntity.ok(recentLogs);
    }
}
