package com.example.deporsm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import com.example.deporsm.repository.UsuarioRepository;

import com.example.deporsm.repository.ReservaRepository;
import com.example.deporsm.repository.InstalacionRepository;
import com.example.deporsm.repository.ObservacionRepository;
import com.example.deporsm.model.Usuario;

import com.example.deporsm.model.Instalacion;

import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@RestController
@RequestMapping("/api")
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
    private ReservaRepository reservaRepository;

    @Autowired
    private InstalacionRepository instalacionRepository;

    @Autowired
    private ObservacionRepository observacionRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @GetMapping("/admin/dashboard")
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



        return dashboardData;
    }



    @GetMapping("/admin/dashboard/charts")
    public Map<String, Object> getDashboardCharts() {
        Map<String, Object> chartsData = new HashMap<>();

        try {
            try {
                // Obtener datos para el gráfico de reservas por instalación
                List<Map<String, Object>> reservationsByFacility = instalacionRepository.findReservationsByFacility();
                List<Map<String, Object>> formattedReservationsByFacility = new ArrayList<>();

                System.out.println("Datos de reservas por instalación (raw): " + reservationsByFacility);

                // Imprimir cada instalación y su conteo para depuración
                if (reservationsByFacility != null) {
                    System.out.println("Desglose de reservas por instalación:");
                    for (Map<String, Object> item : reservationsByFacility) {
                        System.out.println("  - " + item.get("nombre") + ": " + item.get("total_reservas"));
                    }
                }

                if (reservationsByFacility != null && !reservationsByFacility.isEmpty()) {
                    for (Map<String, Object> item : reservationsByFacility) {
                        Map<String, Object> formattedItem = new HashMap<>();
                        formattedItem.put("name", item.get("nombre"));
                        // Asegurarse de que el valor sea un número
                        Object totalReservas = item.get("total_reservas");
                        if (totalReservas instanceof Number) {
                            formattedItem.put("value", totalReservas);
                        } else if (totalReservas != null) {
                            try {
                                formattedItem.put("value", Long.parseLong(totalReservas.toString()));
                            } catch (NumberFormatException e) {
                                formattedItem.put("value", 0);
                            }
                        } else {
                            formattedItem.put("value", 0);
                        }
                        formattedReservationsByFacility.add(formattedItem);
                    }
                }

                // Si no hay datos, agregar al menos una instalación con valor 0
                if (formattedReservationsByFacility.isEmpty()) {
                    // Intentar obtener algunas instalaciones activas
                    try {
                        List<Instalacion> instalaciones = instalacionRepository.findByActivoTrue();
                        if (instalaciones != null && !instalaciones.isEmpty()) {
                            // Mostrar hasta 5 instalaciones activas con valor 0
                            int count = 0;
                            for (Instalacion instalacion : instalaciones) {
                                Map<String, Object> emptyItem = new HashMap<>();
                                emptyItem.put("name", instalacion.getNombre());
                                emptyItem.put("value", 0);
                                formattedReservationsByFacility.add(emptyItem);
                                count++;
                                if (count >= 5) break;
                            }
                        } else {
                            // Si no hay instalaciones activas, mostrar mensaje genérico
                            Map<String, Object> emptyItem = new HashMap<>();
                            emptyItem.put("name", "Sin reservas");
                            emptyItem.put("value", 0);
                            formattedReservationsByFacility.add(emptyItem);
                        }
                    } catch (Exception e) {
                        // En caso de error, mostrar mensaje genérico
                        Map<String, Object> emptyItem = new HashMap<>();
                        emptyItem.put("name", "Sin reservas");
                        emptyItem.put("value", 0);
                        formattedReservationsByFacility.add(emptyItem);
                    }
                }

                chartsData.put("reservationsByFacility", formattedReservationsByFacility);
            } catch (Exception e) {
                System.err.println("Error al obtener datos de reservas por instalación: " + e.getMessage());
                // En caso de error, proporcionar datos mínimos
                List<Map<String, Object>> emptyList = new ArrayList<>();
                Map<String, Object> emptyItem = new HashMap<>();
                emptyItem.put("name", "Error al cargar datos");
                emptyItem.put("value", 0);
                emptyList.add(emptyItem);
                chartsData.put("reservationsByFacility", emptyList);
            }

            // Obtener datos para el gráfico de ingresos diarios
            List<Map<String, Object>> incomeByMonth = reservaRepository.findIncomeByMonth();
            List<Map<String, Object>> formattedIncomeByMonth = new ArrayList<>();

            System.out.println("Datos de ingresos diarios (raw): " + incomeByMonth);

            // Imprimir cada día y su ingreso para depuración
            if (incomeByMonth != null) {
                System.out.println("Desglose de ingresos diarios:");
                for (Map<String, Object> item : incomeByMonth) {
                    System.out.println("  - " + item.get("fecha") + ": " + item.get("total_ingresos"));
                }
            }

            if (incomeByMonth != null && !incomeByMonth.isEmpty()) {
                for (Map<String, Object> item : incomeByMonth) {
                    Map<String, Object> formattedItem = new HashMap<>();
                    formattedItem.put("name", item.get("fecha"));

                    // Asegurarse de que el valor sea un número
                    Object totalIngresos = item.get("total_ingresos");
                    if (totalIngresos instanceof Number) {
                        // Redondear a 2 decimales para mejor visualización
                        double value = ((Number) totalIngresos).doubleValue();
                        formattedItem.put("value", Math.round(value * 100) / 100.0);
                    } else if (totalIngresos != null) {
                        try {
                            double value = Double.parseDouble(totalIngresos.toString());
                            formattedItem.put("value", Math.round(value * 100) / 100.0);
                        } catch (NumberFormatException e) {
                            formattedItem.put("value", 0);
                        }
                    } else {
                        formattedItem.put("value", 0);
                    }

                    formattedIncomeByMonth.add(formattedItem);
                }
            } else {
                // Si no hay datos, agregar algunos puntos de ejemplo para los últimos 30 días
                LocalDate today = LocalDate.now();
                for (int i = 29; i >= 0; i--) {
                    LocalDate date = today.minusDays(i);
                    Map<String, Object> emptyItem = new HashMap<>();
                    emptyItem.put("name", date.format(DateTimeFormatter.ofPattern("dd/MM")));
                    emptyItem.put("value", 0);
                    formattedIncomeByMonth.add(emptyItem);
                }
            }
            chartsData.put("incomeByMonth", formattedIncomeByMonth);

            // Obtener datos para el gráfico de ingresos por instalación
            try {
                List<Map<String, Object>> incomeByFacility = reservaRepository.findIncomeByFacility();
                List<Map<String, Object>> formattedIncomeByFacility = new ArrayList<>();

                System.out.println("Datos de ingresos por instalación (raw): " + incomeByFacility);

                // Imprimir cada instalación y su ingreso para depuración
                if (incomeByFacility != null) {
                    System.out.println("Desglose de ingresos por instalación:");
                    for (Map<String, Object> item : incomeByFacility) {
                        System.out.println("  - " + item.get("nombre") + ": " + item.get("total_ingresos"));
                    }
                }

                if (incomeByFacility != null && !incomeByFacility.isEmpty()) {
                    for (Map<String, Object> item : incomeByFacility) {
                        Map<String, Object> formattedItem = new HashMap<>();
                        formattedItem.put("name", item.get("nombre"));
                        // Asegurarse de que el valor sea un número
                        Object totalIngresos = item.get("total_ingresos");
                        if (totalIngresos instanceof Number) {
                            formattedItem.put("value", totalIngresos);
                        } else if (totalIngresos != null) {
                            try {
                                formattedItem.put("value", Double.parseDouble(totalIngresos.toString()));
                            } catch (NumberFormatException e) {
                                formattedItem.put("value", 0);
                            }
                        } else {
                            formattedItem.put("value", 0);
                        }
                        formattedIncomeByFacility.add(formattedItem);
                    }
                }

                // Si no hay datos, agregar al menos una instalación con valor 0
                if (formattedIncomeByFacility.isEmpty()) {
                    try {
                        List<Instalacion> instalaciones = instalacionRepository.findByActivoTrue();
                        if (instalaciones != null && !instalaciones.isEmpty()) {
                            // Mostrar hasta 5 instalaciones activas con valor 0
                            int count = 0;
                            for (Instalacion instalacion : instalaciones) {
                                Map<String, Object> emptyItem = new HashMap<>();
                                emptyItem.put("name", instalacion.getNombre());
                                emptyItem.put("value", 0);
                                formattedIncomeByFacility.add(emptyItem);
                                count++;
                                if (count >= 5) break;
                            }
                        } else {
                            // Si no hay instalaciones activas, mostrar mensaje genérico
                            Map<String, Object> emptyItem = new HashMap<>();
                            emptyItem.put("name", "Sin ingresos");
                            emptyItem.put("value", 0);
                            formattedIncomeByFacility.add(emptyItem);
                        }
                    } catch (Exception e) {
                        // En caso de error, mostrar mensaje genérico
                        Map<String, Object> emptyItem = new HashMap<>();
                        emptyItem.put("name", "Sin ingresos");
                        emptyItem.put("value", 0);
                        formattedIncomeByFacility.add(emptyItem);
                    }
                }

                chartsData.put("incomeByFacility", formattedIncomeByFacility);
            } catch (Exception e) {
                System.err.println("Error al obtener datos de ingresos por instalación: " + e.getMessage());
                // En caso de error, proporcionar datos mínimos
                List<Map<String, Object>> emptyList = new ArrayList<>();
                Map<String, Object> emptyItem = new HashMap<>();
                emptyItem.put("name", "Error al cargar datos");
                emptyItem.put("value", 0);
                emptyList.add(emptyItem);
                chartsData.put("incomeByFacility", emptyList);
            }

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

            // No necesitamos datos de uso por hora para la interfaz actual
            // Proporcionamos un array vacío para evitar errores en el frontend
            List<Map<String, Object>> formattedUsageByHour = new ArrayList<>();
            Map<String, Object> emptyItem = new HashMap<>();
            emptyItem.put("name", "Sin datos");
            emptyItem.put("value", 0);
            formattedUsageByHour.add(emptyItem);
            chartsData.put("usageByHour", formattedUsageByHour);

            // Obtener datos para el gráfico de reservas por estado
            List<Map<String, Object>> reservationsByStatus = reservaRepository.findReservationsByStatus();
            List<Map<String, Object>> formattedReservationsByStatus = new ArrayList<>();

            for (Map<String, Object> item : reservationsByStatus) {
                Map<String, Object> formattedItem = new HashMap<>();
                String estado = (String) item.get("name");
                // Capitalizar primera letra del estado
                if (estado != null && !estado.isEmpty()) {
                    estado = estado.substring(0, 1).toUpperCase() + estado.substring(1);
                }
                formattedItem.put("name", estado);
                formattedItem.put("value", item.get("value"));
                formattedReservationsByStatus.add(formattedItem);
            }
            chartsData.put("reservationsByStatus", formattedReservationsByStatus);

            // Obtener datos para el gráfico de observaciones por instalación
            try {
                // Consulta para obtener observaciones por instalación
                List<Map<String, Object>> observacionesPorInstalacion = observacionRepository.findObservacionesPorInstalacion();
                List<Map<String, Object>> formattedObservacionesPorInstalacion = new ArrayList<>();

                if (observacionesPorInstalacion != null && !observacionesPorInstalacion.isEmpty()) {
                    for (Map<String, Object> item : observacionesPorInstalacion) {
                        Map<String, Object> formattedItem = new HashMap<>();
                        formattedItem.put("name", item.get("instalacion"));
                        formattedItem.put("value", item.get("cantidad"));
                        formattedObservacionesPorInstalacion.add(formattedItem);
                    }
                } else {
                    // Si no hay datos, agregar un elemento vacío
                    formattedObservacionesPorInstalacion.add(createChartItem("Sin observaciones", 0));
                }

                chartsData.put("observacionesPorInstalacion", formattedObservacionesPorInstalacion);
            } catch (Exception e) {
                System.err.println("Error al obtener observaciones por instalación: " + e.getMessage());
                List<Map<String, Object>> emptyList = new ArrayList<>();
                emptyList.add(createChartItem("Sin datos", 0));
                chartsData.put("observacionesPorInstalacion", emptyList);
            }

            // Obtener datos para el gráfico de estado de mantenimientos
            try {
                // Consulta para obtener estado de mantenimientos
                List<Map<String, Object>> estadoMantenimientos = observacionRepository.findEstadoMantenimientos();
                List<Map<String, Object>> formattedEstadoMantenimientos = new ArrayList<>();

                if (estadoMantenimientos != null && !estadoMantenimientos.isEmpty()) {
                    for (Map<String, Object> item : estadoMantenimientos) {
                        Map<String, Object> formattedItem = new HashMap<>();
                        String estado = (String) item.get("estado");
                        // Capitalizar primera letra del estado
                        if (estado != null && !estado.isEmpty()) {
                            estado = estado.substring(0, 1).toUpperCase() + estado.substring(1);
                        }
                        formattedItem.put("name", estado);
                        formattedItem.put("value", item.get("cantidad"));
                        formattedEstadoMantenimientos.add(formattedItem);
                    }
                } else {
                    // Si no hay datos, agregar elementos con los estados posibles
                    formattedEstadoMantenimientos.add(createChartItem("Programado", 0));
                    formattedEstadoMantenimientos.add(createChartItem("En progreso", 0));
                    formattedEstadoMantenimientos.add(createChartItem("Completado", 0));
                    formattedEstadoMantenimientos.add(createChartItem("Cancelado", 0));
                }

                chartsData.put("estadoMantenimientos", formattedEstadoMantenimientos);
            } catch (Exception e) {
                System.err.println("Error al obtener estado de mantenimientos: " + e.getMessage());
                List<Map<String, Object>> emptyList = new ArrayList<>();
                emptyList.add(createChartItem("Programado", 0));
                emptyList.add(createChartItem("En progreso", 0));
                emptyList.add(createChartItem("Completado", 0));
                emptyList.add(createChartItem("Cancelado", 0));
                chartsData.put("estadoMantenimientos", emptyList);
            }

        } catch (Exception e) {
            e.printStackTrace();
            // Registrar el error detallado
            System.err.println("Error al obtener datos de gráficos: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("Causa: " + e.getCause().getMessage());
            }

            // Crear un mensaje de error detallado
            StringBuilder errorDetail = new StringBuilder();
            errorDetail.append("Error al obtener datos de gráficos: ").append(e.getMessage());

            if (e.getCause() != null) {
                errorDetail.append("\nCausa: ").append(e.getCause().getMessage());
            }

            // Incluir stack trace para depuración
            errorDetail.append("\nStack trace: ");
            for (StackTraceElement element : e.getStackTrace()) {
                errorDetail.append("\n  ").append(element.toString());
                // Limitar a las primeras 10 líneas del stack trace
                if (errorDetail.toString().split("\n").length > 10) {
                    errorDetail.append("\n  ...");
                    break;
                }
            }

            chartsData.put("error", errorDetail.toString());

            // Proporcionar datos mínimos para que la interfaz no se rompa
            // Reservas por instalación (vacío pero válido)
            List<Map<String, Object>> emptyReservationsByFacility = new ArrayList<>();
            emptyReservationsByFacility.add(createChartItem("Sin datos", 0));
            chartsData.put("reservationsByFacility", emptyReservationsByFacility);

            // Ingresos mensuales (vacío pero válido)
            List<Map<String, Object>> emptyIncomeByMonth = new ArrayList<>();
            emptyIncomeByMonth.add(createChartItem("Sin datos", 0));
            chartsData.put("incomeByMonth", emptyIncomeByMonth);

            // Ingresos por instalación (vacío pero válido)
            List<Map<String, Object>> emptyIncomeByFacility = new ArrayList<>();
            emptyIncomeByFacility.add(createChartItem("Sin datos", 0));
            chartsData.put("incomeByFacility", emptyIncomeByFacility);

            // Reservas por día (vacío pero válido)
            List<Map<String, Object>> emptyReservationsByDay = new ArrayList<>();
            emptyReservationsByDay.add(createChartItem("Sin datos", 0));
            chartsData.put("reservationsByDay", emptyReservationsByDay);

            // Uso por hora (vacío pero válido)
            List<Map<String, Object>> emptyUsageByHour = new ArrayList<>();
            emptyUsageByHour.add(createChartItem("Sin datos", 0));
            chartsData.put("usageByHour", emptyUsageByHour);

            // Reservas por estado (con los 4 estados pero valores en 0)
            List<Map<String, Object>> emptyReservationsByStatus = new ArrayList<>();
            emptyReservationsByStatus.add(createChartItem("Pendientes", 0));
            emptyReservationsByStatus.add(createChartItem("Confirmadas", 0));
            emptyReservationsByStatus.add(createChartItem("Completadas", 0));
            emptyReservationsByStatus.add(createChartItem("Canceladas", 0));
            chartsData.put("reservationsByStatus", emptyReservationsByStatus);

            // Observaciones por instalación (vacío pero válido)
            List<Map<String, Object>> emptyObservacionesPorInstalacion = new ArrayList<>();
            emptyObservacionesPorInstalacion.add(createChartItem("Sin datos", 0));
            chartsData.put("observacionesPorInstalacion", emptyObservacionesPorInstalacion);

            // Estado de mantenimientos (con los 4 estados pero valores en 0)
            List<Map<String, Object>> emptyEstadoMantenimientos = new ArrayList<>();
            emptyEstadoMantenimientos.add(createChartItem("Programado", 0));
            emptyEstadoMantenimientos.add(createChartItem("En progreso", 0));
            emptyEstadoMantenimientos.add(createChartItem("Completado", 0));
            emptyEstadoMantenimientos.add(createChartItem("Cancelado", 0));
            chartsData.put("estadoMantenimientos", emptyEstadoMantenimientos);
        }

        return chartsData;
    }

    private Map<String, Object> createChartItem(String name, Number value) {
        Map<String, Object> item = new HashMap<>();
        item.put("name", name);
        item.put("value", value);
        return item;
    }

    /**
     * Dashboard específico para coordinadores
     */
    @GetMapping("/coordinador/dashboard/{coordinadorId}")
    public Map<String, Object> getCoordinadorDashboardData(@PathVariable Integer coordinadorId) {
        Map<String, Object> dashboardData = new HashMap<>();

        try {
            // Verificar que el coordinador existe
            Optional<Usuario> coordinadorOpt = usuarioRepository.findById(coordinadorId);
            if (!coordinadorOpt.isPresent()) {
                dashboardData.put("error", "Coordinador no encontrado");
                return dashboardData;
            }

            Usuario coordinador = coordinadorOpt.get();
            if (coordinador.getRol().getId() != 3) {
                dashboardData.put("error", "El usuario no es un coordinador");
                return dashboardData;
            }

            // Obtener estadísticas básicas del coordinador
            Map<String, Object> stats = new HashMap<>();

            // Total de instalaciones asignadas
            String query1 = """
                SELECT COUNT(DISTINCT ci.instalacion_id)
                FROM coordinadores_instalaciones ci
                WHERE ci.usuario_id = :coordinadorId
            """;
            Long totalFacilities = (Long) entityManager.createNativeQuery(query1)
                .setParameter("coordinadorId", coordinadorId)
                .getSingleResult();
            stats.put("totalFacilities", totalFacilities != null ? totalFacilities.intValue() : 0);

            // Visitas pendientes (programadas para hoy y futuras)
            String query2 = """
                SELECT COUNT(*)
                FROM horarios_coordinadores hc
                JOIN coordinadores_instalaciones ci ON hc.coordinador_instalacion_id = ci.id
                WHERE ci.usuario_id = :coordinadorId
                AND CASE
                    WHEN hc.dia_semana = 'LUNES' THEN WEEKDAY(CURDATE()) = 0
                    WHEN hc.dia_semana = 'MARTES' THEN WEEKDAY(CURDATE()) = 1
                    WHEN hc.dia_semana = 'MIERCOLES' THEN WEEKDAY(CURDATE()) = 2
                    WHEN hc.dia_semana = 'JUEVES' THEN WEEKDAY(CURDATE()) = 3
                    WHEN hc.dia_semana = 'VIERNES' THEN WEEKDAY(CURDATE()) = 4
                    WHEN hc.dia_semana = 'SABADO' THEN WEEKDAY(CURDATE()) = 5
                    WHEN hc.dia_semana = 'DOMINGO' THEN WEEKDAY(CURDATE()) = 6
                    ELSE FALSE
                END
            """;
            Long pendingVisits = (Long) entityManager.createNativeQuery(query2)
                .setParameter("coordinadorId", coordinadorId)
                .getSingleResult();
            stats.put("pendingVisits", pendingVisits != null ? pendingVisits.intValue() : 0);

            // Visitas completadas (asistencias registradas)
            String query3 = """
                SELECT COUNT(*)
                FROM asistencias_coordinadores ac
                WHERE ac.coordinador_id = :coordinadorId
                AND ac.estado_entrada IN ('a-tiempo', 'tarde')
            """;
            Long completedVisits = (Long) entityManager.createNativeQuery(query3)
                .setParameter("coordinadorId", coordinadorId)
                .getSingleResult();
            stats.put("completedVisits", completedVisits != null ? completedVisits.intValue() : 0);

            // Observaciones pendientes de las instalaciones asignadas al coordinador
            String query4 = """
                SELECT COUNT(*)
                FROM observaciones o
                JOIN coordinadores_instalaciones ci ON o.instalacion_id = ci.instalacion_id
                WHERE ci.usuario_id = :coordinadorId
                AND o.estado = 'pendiente'
            """;
            Long pendingObservations = (Long) entityManager.createNativeQuery(query4)
                .setParameter("coordinadorId", coordinadorId)
                .getSingleResult();
            stats.put("pendingObservations", pendingObservations != null ? pendingObservations.intValue() : 0);

            dashboardData.put("stats", stats);

            // Obtener próximas visitas programadas (usando zona horaria de Perú)
            String queryVisitas = """
                SELECT
                    hc.id,
                    ci.instalacion_id as facilityId,
                    i.nombre as facilityName,
                    i.ubicacion as location,
                    DATE_FORMAT(CONVERT_TZ(NOW(), @@session.time_zone, '-05:00'), '%Y-%m-%d') as date,
                    TIME_FORMAT(hc.hora_inicio, '%H:%i') as scheduledTime,
                    TIME_FORMAT(hc.hora_fin, '%H:%i') as scheduledEndTime,
                    'pendiente' as status
                FROM horarios_coordinadores hc
                JOIN coordinadores_instalaciones ci ON hc.coordinador_instalacion_id = ci.id
                JOIN instalaciones i ON ci.instalacion_id = i.id
                WHERE ci.usuario_id = :coordinadorId
                AND CASE
                    WHEN hc.dia_semana = 'LUNES' THEN WEEKDAY(CONVERT_TZ(NOW(), @@session.time_zone, '-05:00')) = 0
                    WHEN hc.dia_semana = 'MARTES' THEN WEEKDAY(CONVERT_TZ(NOW(), @@session.time_zone, '-05:00')) = 1
                    WHEN hc.dia_semana = 'MIERCOLES' THEN WEEKDAY(CONVERT_TZ(NOW(), @@session.time_zone, '-05:00')) = 2
                    WHEN hc.dia_semana = 'JUEVES' THEN WEEKDAY(CONVERT_TZ(NOW(), @@session.time_zone, '-05:00')) = 3
                    WHEN hc.dia_semana = 'VIERNES' THEN WEEKDAY(CONVERT_TZ(NOW(), @@session.time_zone, '-05:00')) = 4
                    WHEN hc.dia_semana = 'SABADO' THEN WEEKDAY(CONVERT_TZ(NOW(), @@session.time_zone, '-05:00')) = 5
                    WHEN hc.dia_semana = 'DOMINGO' THEN WEEKDAY(CONVERT_TZ(NOW(), @@session.time_zone, '-05:00')) = 6
                    ELSE FALSE
                END
                ORDER BY hc.hora_inicio
                LIMIT 3
            """;

            @SuppressWarnings("unchecked")
            List<Object[]> visitasResults = entityManager.createNativeQuery(queryVisitas)
                .setParameter("coordinadorId", coordinadorId)
                .getResultList();

            List<Map<String, Object>> upcomingVisits = new ArrayList<>();
            for (Object[] row : visitasResults) {
                Map<String, Object> visit = new HashMap<>();
                visit.put("id", row[0]);
                visit.put("facilityId", row[1]);
                visit.put("facilityName", row[2]);
                visit.put("location", row[3]);
                visit.put("date", row[4].toString());
                visit.put("scheduledTime", row[5].toString());
                visit.put("scheduledEndTime", row[6].toString());
                visit.put("status", row[7]);
                upcomingVisits.add(visit);
            }
            dashboardData.put("upcomingVisits", upcomingVisits);

            // Obtener observaciones recientes de las instalaciones asignadas al coordinador
            String queryObservaciones = """
                SELECT
                    o.id,
                    o.instalacion_id as facilityId,
                    i.nombre as facilityName,
                    i.ubicacion as location,
                    o.descripcion as description,
                    o.estado as status,
                    DATE_FORMAT(CONVERT_TZ(o.created_at, @@session.time_zone, '-05:00'), '%Y-%m-%d') as date,
                    o.prioridad as priority
                FROM observaciones o
                JOIN instalaciones i ON o.instalacion_id = i.id
                JOIN coordinadores_instalaciones ci ON o.instalacion_id = ci.instalacion_id
                WHERE ci.usuario_id = :coordinadorId
                ORDER BY o.created_at DESC
                LIMIT 3
            """;

            @SuppressWarnings("unchecked")
            List<Object[]> observacionesResults = entityManager.createNativeQuery(queryObservaciones)
                .setParameter("coordinadorId", coordinadorId)
                .getResultList();

            List<Map<String, Object>> recentObservations = new ArrayList<>();
            for (Object[] row : observacionesResults) {
                Map<String, Object> observation = new HashMap<>();
                observation.put("id", row[0]);
                observation.put("facilityId", row[1]);
                observation.put("facilityName", row[2]);
                observation.put("location", row[3]);
                observation.put("description", row[4]);
                observation.put("status", row[5]);
                observation.put("date", row[6]);
                observation.put("priority", row[7]);
                recentObservations.add(observation);
            }
            dashboardData.put("recentObservations", recentObservations);

            // Obtener estadísticas de asistencia
            String queryAsistencia = """
                SELECT
                    SUM(CASE WHEN estado_entrada = 'a-tiempo' THEN 1 ELSE 0 END) as onTime,
                    SUM(CASE WHEN estado_entrada = 'tarde' THEN 1 ELSE 0 END) as late,
                    SUM(CASE WHEN estado_entrada = 'no-asistio' THEN 1 ELSE 0 END) as missed,
                    COUNT(*) as total
                FROM asistencias_coordinadores
                WHERE coordinador_id = :coordinadorId
            """;

            Object[] asistenciaResult = (Object[]) entityManager.createNativeQuery(queryAsistencia)
                .setParameter("coordinadorId", coordinadorId)
                .getSingleResult();

            Map<String, Object> attendanceStats = new HashMap<>();
            attendanceStats.put("onTime", asistenciaResult[0] != null ? ((Number) asistenciaResult[0]).intValue() : 0);
            attendanceStats.put("late", asistenciaResult[1] != null ? ((Number) asistenciaResult[1]).intValue() : 0);
            attendanceStats.put("missed", asistenciaResult[2] != null ? ((Number) asistenciaResult[2]).intValue() : 0);
            attendanceStats.put("total", asistenciaResult[3] != null ? ((Number) asistenciaResult[3]).intValue() : 0);

            dashboardData.put("attendanceStats", attendanceStats);

        } catch (Exception e) {
            System.err.println("Error al obtener datos del dashboard del coordinador: " + e.getMessage());
            e.printStackTrace();
            dashboardData.put("error", "Error al cargar datos del dashboard: " + e.getMessage());
        }

        return dashboardData;
    }
}
