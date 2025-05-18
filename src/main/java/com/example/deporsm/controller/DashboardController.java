package com.example.deporsm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import com.example.deporsm.repository.UsuarioRepository;
import com.example.deporsm.repository.LogActividadRepository;
import com.example.deporsm.model.Usuario;
import com.example.deporsm.model.LogActividad;
import com.example.deporsm.model.Rol;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(
  origins = {
    "https://deporsm-apiwith-1035693188565.us-central1.run.app",
    "https://frontend-depor-sm-leo.vercel.app",
    "http://localhost:3000"
  },
  allowCredentials = "true"
)
public class DashboardController {

    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private LogActividadRepository logActividadRepository;

    @GetMapping("/dashboard")
    public Map<String, Object> getDashboardData() {
        Map<String, Object> dashboardData = new HashMap<>();
        Map<String, Integer> monthlyChanges = new HashMap<>();
          // Get user stats
        List<Usuario> users = usuarioRepository.findAll();
        int totalUsers = users.size(); // Total de usuarios sin importar si están activos o no
        long adminUsers = users.stream()
            .filter(u -> u.getRol().getId() == 2 && u.getActivo())
            .count();
        long coordUsers = users.stream()
            .filter(u -> u.getRol().getId() == 3 && u.getActivo())
            .count();        long vecinoUsers = users.stream()
            .filter(u -> u.getRol().getId() == 4)
            .count();
        
        dashboardData.put("totalUsers", totalUsers);
        dashboardData.put("adminUsers", adminUsers);
        dashboardData.put("coordUsers", coordUsers);
        dashboardData.put("vecinoUsers", vecinoUsers);

        // Calculate monthly changes
        monthlyChanges.put("totalUsers", usuarioRepository.getMonthlyGrowthPercentage());
        monthlyChanges.put("adminUsers", usuarioRepository.getMonthlyGrowthPercentageByRole(2));
        monthlyChanges.put("coordUsers", usuarioRepository.getMonthlyGrowthPercentageByRole(3));
        monthlyChanges.put("vecinoUsers", usuarioRepository.getMonthlyGrowthPercentageByRole(4));
        dashboardData.put("monthlyChanges", monthlyChanges);

        // Get user distribution for chart
        Map<String, Integer> userDistribution = new HashMap<>();
        userDistribution.put("Administradores", (int) adminUsers);
        userDistribution.put("Coordinadores", (int) coordUsers);
        userDistribution.put("Vecinos", (int) vecinoUsers);
        dashboardData.put("userDistribution", userDistribution);

        // Add recent activity if using activity logging
        if (logActividadRepository != null) {
            // Get recent activity from log_actividades
            List<LogActividad> recentActivities = logActividadRepository.findTop100ByOrderByCreatedAtDesc();
            List<Map<String, Object>> recentActivity = new ArrayList<>();
            
            // Take only the first 10 activities
            recentActivities.stream().limit(10).forEach(log -> {
                Map<String, Object> activity = new HashMap<>();
                activity.put("id", log.getId());
                
                if (log.getUsuario() != null) {
                    activity.put("user", log.getUsuario().getNombre() + " " + log.getUsuario().getApellidos());
                    activity.put("userType", getRoleString(log.getUsuario().getRol().getId()));
                } else {
                    activity.put("user", "Usuario Desconocido");
                    activity.put("userType", "No Identificado");
                }
                
                activity.put("action", getActionDescription(log.getAccion()));
                activity.put("type", log.getAccion());
                activity.put("date", formatTimeAgo(log.getCreatedAt()));
                activity.put("status", log.getEstado());
                
                recentActivity.add(activity);
            });

            dashboardData.put("recentActivity", recentActivity);
        }

        return dashboardData;
    }
    
    private String getRoleString(int roleId) {
        switch (roleId) {
            case 1: return "Superadmin";
            case 2: return "Administrador";
            case 3: return "Coordinador";
            case 4: return "Vecino";
            default: return "Usuario";
        }
    }

    private String getActionDescription(String action) {
        switch (action.toLowerCase()) {
            case "login": return "Inicio sesión";
            case "logout": return "Cerró sesión";
            case "create": return "Creó un recurso";
            case "update": return "Actualizó un recurso";
            case "delete": return "Eliminó un recurso";
            default: return action;
        }
    }

    private String formatTimeAgo(Date date) {
        if (date == null) return "fecha desconocida";
        
        long diffInMillis = System.currentTimeMillis() - date.getTime();
        long seconds = diffInMillis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return days + " día" + (days > 1 ? "s" : "") + " atrás";
        } else if (hours > 0) {
            return hours + " hora" + (hours > 1 ? "s" : "") + " atrás";
        } else if (minutes > 0) {
            return minutes + " minuto" + (minutes > 1 ? "s" : "") + " atrás";
        } else {
            return seconds + " segundo" + (seconds != 1 ? "s" : "") + " atrás";
        }
    }
}
