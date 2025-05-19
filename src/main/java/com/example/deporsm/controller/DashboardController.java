package com.example.deporsm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import com.example.deporsm.repository.UsuarioRepository;
import com.example.deporsm.repository.LogActividadRepository;
import com.example.deporsm.repository.ReservaRepository;
import com.example.deporsm.repository.InstalacionRepository;
import com.example.deporsm.repository.ObservacionRepository;
import com.example.deporsm.model.Usuario;
import com.example.deporsm.model.LogActividad;

import java.util.*;

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

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private InstalacionRepository instalacionRepository;

    @Autowired
    private ObservacionRepository observacionRepository;

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

    @GetMapping("/dashboard/charts")
    public Map<String, Object> getDashboardCharts() {
        Map<String, Object> chartsData = new HashMap<>();

        try {
            // Obtener datos para el gráfico de reservas por instalación
            List<Map<String, Object>> reservationsByFacility = instalacionRepository.findReservationsByFacility();
            List<Map<String, Object>> formattedReservationsByFacility = new ArrayList<>();

            for (Map<String, Object> item : reservationsByFacility) {
                Map<String, Object> formattedItem = new HashMap<>();
                formattedItem.put("name", item.get("nombre"));
                formattedItem.put("value", item.get("total_reservas"));
                formattedReservationsByFacility.add(formattedItem);
            }
            chartsData.put("reservationsByFacility", formattedReservationsByFacility);

            // Obtener datos para el gráfico de ingresos mensuales
            List<Map<String, Object>> incomeByMonth = reservaRepository.findIncomeByMonth();
            List<Map<String, Object>> formattedIncomeByMonth = new ArrayList<>();

            for (Map<String, Object> item : incomeByMonth) {
                Map<String, Object> formattedItem = new HashMap<>();
                formattedItem.put("name", item.get("mes"));
                formattedItem.put("value", item.get("total_ingresos"));
                formattedIncomeByMonth.add(formattedItem);
            }
            chartsData.put("incomeByMonth", formattedIncomeByMonth);

            // Obtener datos para el gráfico de reservas por día de la semana
            List<Map<String, Object>> reservationsByDay = reservaRepository.findReservationsByDayOfWeek();
            List<Map<String, Object>> formattedReservationsByDay = new ArrayList<>();

            for (Map<String, Object> item : reservationsByDay) {
                Map<String, Object> formattedItem = new HashMap<>();
                formattedItem.put("name", item.get("dia_semana"));
                formattedItem.put("value", item.get("total_reservas"));
                formattedReservationsByDay.add(formattedItem);
            }
            chartsData.put("reservationsByDay", formattedReservationsByDay);

            // Obtener datos para el gráfico de uso por hora
            List<Map<String, Object>> usageByHour = reservaRepository.findUsageByHour();
            List<Map<String, Object>> formattedUsageByHour = new ArrayList<>();

            for (Map<String, Object> item : usageByHour) {
                Map<String, Object> formattedItem = new HashMap<>();
                formattedItem.put("name", item.get("hora"));
                formattedItem.put("value", item.get("total_reservas"));
                formattedUsageByHour.add(formattedItem);
            }
            chartsData.put("usageByHour", formattedUsageByHour);

        } catch (Exception e) {
            e.printStackTrace();
            // En caso de error, devolver datos de ejemplo
            chartsData.put("error", "Error al obtener datos de gráficos: " + e.getMessage());

            // Datos de ejemplo para reservas por instalación
            List<Map<String, Object>> sampleReservationsByFacility = new ArrayList<>();
            sampleReservationsByFacility.add(createChartItem("Piscina Municipal", 65));
            sampleReservationsByFacility.add(createChartItem("Cancha de Fútbol (Grass)", 85));
            sampleReservationsByFacility.add(createChartItem("Gimnasio Municipal", 45));
            sampleReservationsByFacility.add(createChartItem("Cancha de Fútbol (Loza)", 35));
            sampleReservationsByFacility.add(createChartItem("Pista de Atletismo", 18));
            chartsData.put("reservationsByFacility", sampleReservationsByFacility);

            // Datos de ejemplo para ingresos mensuales
            List<Map<String, Object>> sampleIncomeByMonth = new ArrayList<>();
            sampleIncomeByMonth.add(createChartItem("Ene", 1200));
            sampleIncomeByMonth.add(createChartItem("Feb", 1350));
            sampleIncomeByMonth.add(createChartItem("Mar", 1500));
            sampleIncomeByMonth.add(createChartItem("Abr", 1650));
            sampleIncomeByMonth.add(createChartItem("May", 1800));
            sampleIncomeByMonth.add(createChartItem("Jun", 1950));
            chartsData.put("incomeByMonth", sampleIncomeByMonth);

            // Datos de ejemplo para reservas por día
            List<Map<String, Object>> sampleReservationsByDay = new ArrayList<>();
            sampleReservationsByDay.add(createChartItem("Lun", 35));
            sampleReservationsByDay.add(createChartItem("Mar", 28));
            sampleReservationsByDay.add(createChartItem("Mié", 32));
            sampleReservationsByDay.add(createChartItem("Jue", 30));
            sampleReservationsByDay.add(createChartItem("Vie", 42));
            sampleReservationsByDay.add(createChartItem("Sáb", 50));
            sampleReservationsByDay.add(createChartItem("Dom", 45));
            chartsData.put("reservationsByDay", sampleReservationsByDay);

            // Datos de ejemplo para uso por hora
            List<Map<String, Object>> sampleUsageByHour = new ArrayList<>();
            sampleUsageByHour.add(createChartItem("8:00", 15));
            sampleUsageByHour.add(createChartItem("9:00", 20));
            sampleUsageByHour.add(createChartItem("10:00", 25));
            sampleUsageByHour.add(createChartItem("11:00", 30));
            sampleUsageByHour.add(createChartItem("12:00", 20));
            sampleUsageByHour.add(createChartItem("13:00", 15));
            sampleUsageByHour.add(createChartItem("14:00", 10));
            sampleUsageByHour.add(createChartItem("15:00", 15));
            sampleUsageByHour.add(createChartItem("16:00", 25));
            sampleUsageByHour.add(createChartItem("17:00", 35));
            sampleUsageByHour.add(createChartItem("18:00", 45));
            sampleUsageByHour.add(createChartItem("19:00", 40));
            sampleUsageByHour.add(createChartItem("20:00", 30));
            chartsData.put("usageByHour", sampleUsageByHour);
        }

        return chartsData;
    }

    private Map<String, Object> createChartItem(String name, Number value) {
        Map<String, Object> item = new HashMap<>();
        item.put("name", name);
        item.put("value", value);
        return item;
    }
}
