package com.example.deporsm.service;

import com.example.deporsm.model.LogActividad;
import com.example.deporsm.repository.LogActividadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class LogActividadService {

    @Autowired
    private LogActividadRepository logActividadRepository;

    public List<LogActividad> findByFilters(String role, String action, String status, String startDate, String endDate) {
        if (role == null && action == null && status == null && startDate == null && endDate == null) {
            return logActividadRepository.findTop100ByOrderByCreatedAtDesc();
        }
        return logActividadRepository.findByFilters(role, action, status, startDate, endDate);
    }

    public List<LogActividad> findMostRecent() {
        return logActividadRepository.findTop100ByOrderByCreatedAtDesc();
    }
}
