package com.example.deporsm.service;

import com.example.deporsm.dto.MantenimientoRequestDTO;
import com.example.deporsm.model.Instalacion;
import com.example.deporsm.model.MantenimientoInstalacion;
import com.example.deporsm.model.Observacion;
import com.example.deporsm.model.Reserva;
import com.example.deporsm.model.Usuario;
import com.example.deporsm.repository.InstalacionRepository;
import com.example.deporsm.repository.MantenimientoInstalacionRepository;
import com.example.deporsm.repository.ObservacionRepository;
import com.example.deporsm.repository.ReservaRepository;
import com.example.deporsm.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class MantenimientoService {

    private final MantenimientoInstalacionRepository mantenimientoRepository;
    private final InstalacionRepository instalacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final ReservaRepository reservaRepository;
    private final ObservacionRepository observacionRepository;
    private final NotificacionService notificacionService;
    private final EntityManager entityManager;

    public MantenimientoService(
            MantenimientoInstalacionRepository mantenimientoRepository,
            InstalacionRepository instalacionRepository,
            UsuarioRepository usuarioRepository,
            ReservaRepository reservaRepository,
            ObservacionRepository observacionRepository,
            NotificacionService notificacionService,
            EntityManager entityManager) {
        this.mantenimientoRepository = mantenimientoRepository;
        this.instalacionRepository = instalacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.reservaRepository = reservaRepository;
        this.observacionRepository = observacionRepository;
        this.notificacionService = notificacionService;
        this.entityManager = entityManager;
    }

    /**
     * Programa un mantenimiento para una instalación y cancela las reservas afectadas
     * @param requestDTO Datos del mantenimiento a programar
     * @return El mantenimiento programado
     */
    @Transactional
    public MantenimientoInstalacion programarMantenimiento(MantenimientoRequestDTO requestDTO) {
        // Verificar que la instalación existe
        Instalacion instalacion = instalacionRepository.findById(requestDTO.getInstalacionId())
                .orElseThrow(() -> new RuntimeException("Instalación no encontrada"));

        // Verificar que el usuario que registra existe
        Usuario registrador = usuarioRepository.findById(requestDTO.getRegistradoPorId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Imprimir información detallada de las fechas recibidas en el servicio
        System.out.println("=== INFORMACIÓN DE FECHAS Y HORAS EN EL SERVICIO ===");
        System.out.println("Fecha inicio recibida: " + requestDTO.getFechaInicio());
        System.out.println("Fecha fin recibida: " + requestDTO.getFechaFin());
        System.out.println("Fecha inicio - Año: " + requestDTO.getFechaInicio().getYear() +
                          ", Mes: " + requestDTO.getFechaInicio().getMonthValue() +
                          ", Día: " + requestDTO.getFechaInicio().getDayOfMonth() +
                          ", Hora: " + requestDTO.getFechaInicio().getHour() +
                          ", Minuto: " + requestDTO.getFechaInicio().getMinute());
        System.out.println("Fecha fin - Año: " + requestDTO.getFechaFin().getYear() +
                          ", Mes: " + requestDTO.getFechaFin().getMonthValue() +
                          ", Día: " + requestDTO.getFechaFin().getDayOfMonth() +
                          ", Hora: " + requestDTO.getFechaFin().getHour() +
                          ", Minuto: " + requestDTO.getFechaFin().getMinute());
        System.out.println("==============================================");

        // Crear una nueva instancia de MantenimientoInstalacion
        MantenimientoInstalacion mantenimiento = new MantenimientoInstalacion();

        // Establecer los datos desde el DTO
        mantenimiento.setInstalacion(instalacion);
        mantenimiento.setRegistradoPor(registrador);
        mantenimiento.setMotivo(requestDTO.getMotivo());
        mantenimiento.setTipo(requestDTO.getTipo());
        mantenimiento.setDescripcion(requestDTO.getDescripcion());
        mantenimiento.setFechaInicio(requestDTO.getFechaInicio());
        mantenimiento.setFechaFin(requestDTO.getFechaFin());
        mantenimiento.setAfectaDisponibilidad(requestDTO.getAfectaDisponibilidad());

        // Establecer fechas de creación y actualización
        LocalDateTime now = LocalDateTime.now();
        mantenimiento.setCreatedAt(now);
        mantenimiento.setUpdatedAt(now);

        // Obtener componentes de fecha y hora para comparaciones más detalladas
        LocalDateTime fechaInicio = mantenimiento.getFechaInicio();
        LocalDateTime fechaFin = mantenimiento.getFechaFin();

        // Comparación detallada para determinar el estado
        boolean esAnteriorAInicio = now.compareTo(fechaInicio) < 0;
        boolean esPosteriorAFin = now.compareTo(fechaFin) >= 0;
        boolean estaEntreFechas = !esAnteriorAInicio && !esPosteriorAFin;

        // Determinar el estado según las comparaciones
        if (esAnteriorAInicio) {
            // Si la fecha/hora actual es anterior a la fecha/hora de inicio
            mantenimiento.setEstado("programado");
        } else if (esPosteriorAFin) {
            // Si la fecha/hora actual es igual o posterior a la fecha/hora de fin
            mantenimiento.setEstado("completado");
        } else if (estaEntreFechas) {
            // Si la fecha/hora actual está entre la fecha/hora de inicio y fin
            mantenimiento.setEstado("en-progreso");
        }

        // Imprimir información de depuración detallada
        System.out.println("=== PROGRAMACIÓN DE MANTENIMIENTO ===");
        System.out.println("Fecha/hora actual: " + now);
        System.out.println("Fecha/hora inicio: " + fechaInicio);
        System.out.println("Fecha/hora fin: " + fechaFin);
        System.out.println("Es anterior a inicio: " + esAnteriorAInicio);
        System.out.println("Es posterior a fin: " + esPosteriorAFin);
        System.out.println("Está entre fechas: " + estaEntreFechas);
        System.out.println("Estado calculado: " + mantenimiento.getEstado());
        System.out.println("====================================");

        // Guardar el mantenimiento
        MantenimientoInstalacion guardado = mantenimientoRepository.save(mantenimiento);

        // Solo cancelar reservas si el mantenimiento afecta la disponibilidad
        if (mantenimiento.getAfectaDisponibilidad() != null && mantenimiento.getAfectaDisponibilidad()) {
            // Buscar reservas afectadas por el mantenimiento
            List<Reserva> reservasAfectadas = buscarReservasAfectadas(
                    instalacion.getId(),
                    mantenimiento.getFechaInicio(),
                    mantenimiento.getFechaFin()
            );

            // Cancelar las reservas afectadas
            for (Reserva reserva : reservasAfectadas) {
                cancelarReservaAutomaticamente(reserva, mantenimiento);
            }
        }

        return guardado;
    }

    /**
     * Programa un mantenimiento para una instalación y cancela las reservas afectadas
     * @param mantenimiento Datos del mantenimiento a programar
     * @return El mantenimiento programado
     */
    @Transactional
    public MantenimientoInstalacion programarMantenimiento(MantenimientoInstalacion mantenimiento) {
        // Verificar que la instalación existe
        Instalacion instalacion = instalacionRepository.findById(mantenimiento.getInstalacion().getId())
                .orElseThrow(() -> new RuntimeException("Instalación no encontrada"));

        // Verificar que el usuario que registra existe
        Usuario registrador = usuarioRepository.findById(mantenimiento.getRegistradoPor().getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Establecer la instalación y el usuario
        mantenimiento.setInstalacion(instalacion);
        mantenimiento.setRegistradoPor(registrador);

        // Establecer fechas de creación y actualización
        LocalDateTime now = LocalDateTime.now();
        mantenimiento.setCreatedAt(now);
        mantenimiento.setUpdatedAt(now);

        // Obtener componentes de fecha y hora para comparaciones más detalladas
        LocalDateTime fechaInicio = mantenimiento.getFechaInicio();
        LocalDateTime fechaFin = mantenimiento.getFechaFin();

        // Comparación detallada para determinar el estado
        boolean esAnteriorAInicio = now.compareTo(fechaInicio) < 0;
        boolean esPosteriorAFin = now.compareTo(fechaFin) >= 0;
        boolean estaEntreFechas = !esAnteriorAInicio && !esPosteriorAFin;

        // Determinar el estado según las comparaciones
        if (esAnteriorAInicio) {
            // Si la fecha/hora actual es anterior a la fecha/hora de inicio
            mantenimiento.setEstado("programado");
        } else if (esPosteriorAFin) {
            // Si la fecha/hora actual es igual o posterior a la fecha/hora de fin
            mantenimiento.setEstado("completado");
        } else if (estaEntreFechas) {
            // Si la fecha/hora actual está entre la fecha/hora de inicio y fin
            mantenimiento.setEstado("en-progreso");
        }

        // Imprimir información de depuración detallada
        System.out.println("=== PROGRAMACIÓN DE MANTENIMIENTO ===");
        System.out.println("Fecha/hora actual: " + now);
        System.out.println("Fecha/hora inicio: " + fechaInicio);
        System.out.println("Fecha/hora fin: " + fechaFin);
        System.out.println("Es anterior a inicio: " + esAnteriorAInicio);
        System.out.println("Es posterior a fin: " + esPosteriorAFin);
        System.out.println("Está entre fechas: " + estaEntreFechas);
        System.out.println("Estado calculado: " + mantenimiento.getEstado());
        System.out.println("====================================");

        // Guardar el mantenimiento
        MantenimientoInstalacion guardado = mantenimientoRepository.save(mantenimiento);

        // Solo cancelar reservas si el mantenimiento afecta la disponibilidad
        if (mantenimiento.getAfectaDisponibilidad() != null && mantenimiento.getAfectaDisponibilidad()) {
            // Buscar reservas afectadas por el mantenimiento
            List<Reserva> reservasAfectadas = buscarReservasAfectadas(
                    instalacion.getId(),
                    mantenimiento.getFechaInicio(),
                    mantenimiento.getFechaFin()
            );

            // Cancelar las reservas afectadas
            for (Reserva reserva : reservasAfectadas) {
                cancelarReservaAutomaticamente(reserva, mantenimiento);
            }
        }

        return guardado;
    }

    /**
     * Busca las reservas que se verán afectadas por un mantenimiento
     */
    private List<Reserva> buscarReservasAfectadas(Integer instalacionId, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        // Imprimir información detallada de las fechas recibidas
        System.out.println("=== INFORMACIÓN DE FECHAS Y HORAS EN BUSCAR RESERVAS AFECTADAS ===");
        System.out.println("Fecha inicio: " + fechaInicio);
        System.out.println("Fecha fin: " + fechaFin);
        System.out.println("Fecha inicio - Año: " + fechaInicio.getYear() +
                          ", Mes: " + fechaInicio.getMonthValue() +
                          ", Día: " + fechaInicio.getDayOfMonth() +
                          ", Hora: " + fechaInicio.getHour() +
                          ", Minuto: " + fechaInicio.getMinute());
        System.out.println("Fecha fin - Año: " + fechaFin.getYear() +
                          ", Mes: " + fechaFin.getMonthValue() +
                          ", Día: " + fechaFin.getDayOfMonth() +
                          ", Hora: " + fechaFin.getHour() +
                          ", Minuto: " + fechaFin.getMinute());

        // Convertir LocalDateTime a Date y Time para la consulta
        LocalDate fechaInicioDate = fechaInicio.toLocalDate();
        LocalDate fechaFinDate = fechaFin.toLocalDate();
        LocalTime horaInicioTime = fechaInicio.toLocalTime();
        LocalTime horaFinTime = fechaFin.toLocalTime();

        System.out.println("Fecha inicio convertida: " + fechaInicioDate);
        System.out.println("Fecha fin convertida: " + fechaFinDate);
        System.out.println("Hora inicio convertida: " + horaInicioTime);
        System.out.println("Hora fin convertida: " + horaFinTime);
        System.out.println("==============================================");

        // Consulta JPQL para encontrar reservas que se solapan con el período de mantenimiento
        String jpql = "SELECT r FROM Reserva r " +
                "WHERE r.instalacion.id = :instalacionId " +
                "AND r.estado != 'cancelada' " +
                "AND (" +
                // Caso 1: Reserva en el mismo día que el mantenimiento (inicio y fin)
                "(r.fecha = :fechaInicio AND r.fecha = :fechaFin " +
                "AND (" +
                "  (r.horaInicio <= :horaInicio AND r.horaFin > :horaInicio) OR " +
                "  (r.horaInicio < :horaFin AND r.horaFin >= :horaFin) OR " +
                "  (r.horaInicio >= :horaInicio AND r.horaFin <= :horaFin)" +
                ")) " +
                // Caso 2: Reserva en el día de inicio del mantenimiento y mantenimiento dura más de un día
                "OR (r.fecha = :fechaInicio AND :fechaInicio < :fechaFin " +
                "AND r.horaFin > :horaInicio) " +
                // Caso 3: Reserva en el día de fin del mantenimiento y mantenimiento comenzó en día anterior
                "OR (r.fecha = :fechaFin AND :fechaInicio < :fechaFin " +
                "AND r.horaInicio < :horaFin) " +
                // Caso 4: Reserva en un día entre el inicio y fin del mantenimiento
                "OR (r.fecha > :fechaInicio AND r.fecha < :fechaFin) " +
                ")";

        List<Reserva> reservasAfectadas = entityManager.createQuery(jpql, Reserva.class)
                .setParameter("instalacionId", instalacionId)
                .setParameter("fechaInicio", Date.valueOf(fechaInicioDate))
                .setParameter("fechaFin", Date.valueOf(fechaFinDate))
                .setParameter("horaInicio", Time.valueOf(horaInicioTime))
                .setParameter("horaFin", Time.valueOf(horaFinTime))
                .getResultList();

        // Imprimir información de depuración
        System.out.println("=== RESERVAS AFECTADAS POR MANTENIMIENTO ===");
        System.out.println("Instalación ID: " + instalacionId);
        System.out.println("Fecha inicio mantenimiento: " + fechaInicioDate + " " + horaInicioTime);
        System.out.println("Fecha fin mantenimiento: " + fechaFinDate + " " + horaFinTime);
        System.out.println("Total reservas afectadas: " + reservasAfectadas.size());

        for (Reserva r : reservasAfectadas) {
            System.out.println("Reserva ID: " + r.getId() +
                              ", Fecha: " + r.getFecha() +
                              ", Hora: " + r.getHoraInicio() + " - " + r.getHoraFin());
        }

        System.out.println("==========================================");

        return reservasAfectadas;
    }

    /**
     * Cancela una reserva automáticamente debido a un mantenimiento programado
     */
    private void cancelarReservaAutomaticamente(Reserva reserva, MantenimientoInstalacion mantenimiento) {
        // Cambiar el estado de la reserva a cancelada
        reserva.setEstado("cancelada");

        // Si hay un campo de motivo, establecerlo
        String motivo = "Cancelada automáticamente debido a mantenimiento programado: " + mantenimiento.getMotivo();
        try {
            reserva.setMotivo(motivo);
        } catch (Exception e) {
            // Si el campo no existe, ignorar
            System.out.println("No se pudo establecer el motivo de cancelación: " + e.getMessage());
        }

        // Guardar la reserva
        reservaRepository.save(reserva);

        // Enviar notificación al usuario
        String mensaje = "Tu reserva para " + reserva.getInstalacion().getNombre() +
                " el día " + reserva.getFecha() + " de " + reserva.getHoraInicio() +
                " a " + reserva.getHoraFin() + " ha sido cancelada debido a un mantenimiento programado.";

        notificacionService.crearNotificacion(
                reserva.getUsuario().getId(),
                "Reserva cancelada por mantenimiento",
                mensaje,
                "reserva"
        );
    }

    /**
     * Actualiza el estado de un mantenimiento según sus fechas y horas
     * @param mantenimiento El mantenimiento a actualizar
     * @return El mantenimiento con el estado actualizado
     */
    @Transactional
    public MantenimientoInstalacion actualizarEstadoMantenimiento(MantenimientoInstalacion mantenimiento) {
        // Si el mantenimiento está cancelado, no cambiar su estado
        if ("cancelado".equals(mantenimiento.getEstado())) {
            return mantenimiento;
        }

        LocalDateTime now = LocalDateTime.now();
        String estadoAnterior = mantenimiento.getEstado();
        String nuevoEstado = estadoAnterior;

        // Obtener componentes de fecha y hora para comparaciones más detalladas
        LocalDateTime fechaInicio = mantenimiento.getFechaInicio();
        LocalDateTime fechaFin = mantenimiento.getFechaFin();

        // Comparación detallada para determinar el estado
        boolean esAnteriorAInicio = now.compareTo(fechaInicio) < 0;
        boolean esPosteriorAFin = now.compareTo(fechaFin) >= 0;
        boolean estaEntreFechas = !esAnteriorAInicio && !esPosteriorAFin;

        // Determinar el estado según las comparaciones
        if (esAnteriorAInicio) {
            // Si la fecha/hora actual es anterior a la fecha/hora de inicio
            nuevoEstado = "programado";
        } else if (esPosteriorAFin) {
            // Si la fecha/hora actual es igual o posterior a la fecha/hora de fin
            nuevoEstado = "completado";
        } else if (estaEntreFechas) {
            // Si la fecha/hora actual está entre la fecha/hora de inicio y fin
            nuevoEstado = "en-progreso";
        }

        // Imprimir información de depuración detallada
        System.out.println("=== ACTUALIZACIÓN DE ESTADO DE MANTENIMIENTO ===");
        System.out.println("ID: " + mantenimiento.getId());
        System.out.println("Fecha/hora actual: " + now);
        System.out.println("Fecha/hora inicio: " + fechaInicio);
        System.out.println("Fecha/hora fin: " + fechaFin);
        System.out.println("Es anterior a inicio: " + esAnteriorAInicio);
        System.out.println("Es posterior a fin: " + esPosteriorAFin);
        System.out.println("Está entre fechas: " + estaEntreFechas);
        System.out.println("Estado anterior: " + estadoAnterior);
        System.out.println("Nuevo estado: " + nuevoEstado);
        System.out.println("==============================================");

        // Siempre guardar el estado actualizado para asegurar que se aplique
        if (!nuevoEstado.equals(estadoAnterior)) {
            mantenimiento.setEstado(nuevoEstado);
            mantenimiento.setUpdatedAt(LocalDateTime.now());
            MantenimientoInstalacion actualizado = mantenimientoRepository.save(mantenimiento);
            System.out.println("Estado actualizado y guardado en la base de datos.");

            // Si el mantenimiento se completó automáticamente, actualizar la observación y la instalación
            if ("completado".equals(nuevoEstado) && !"completado".equals(estadoAnterior)) {
                completarMantenimientoYActualizarObservacion(actualizado);
            }

            return actualizado;
        }

        return mantenimiento;
    }

    /**
     * Tarea programada para actualizar automáticamente el estado de todos los mantenimientos
     * Se ejecuta cada 1 minuto
     */
    @Scheduled(fixedRate = 60000) // 1 minuto = 60,000 ms
    @Transactional
    public void actualizarEstadosMantenimientos() {
        try {
            System.out.println("\n=== INICIANDO ACTUALIZACIÓN AUTOMÁTICA DE ESTADOS ===");
            System.out.println("Fecha/hora actual: " + LocalDateTime.now());

            // Obtener todos los mantenimientos
            List<MantenimientoInstalacion> mantenimientos = mantenimientoRepository.findAll();
            System.out.println("Total de mantenimientos encontrados: " + mantenimientos.size());

            // Actualizar todos los mantenimientos que no estén en estados finales
            int actualizados = 0;
            for (MantenimientoInstalacion m : mantenimientos) {
                if (!"cancelado".equals(m.getEstado()) && !"completado".equals(m.getEstado())) {
                    String estadoAnterior = m.getEstado();
                    actualizarEstadoMantenimiento(m);
                    if (!estadoAnterior.equals(m.getEstado())) {
                        actualizados++;
                    }
                }
            }

            System.out.println("Mantenimientos actualizados: " + actualizados);
            System.out.println("Actualización automática completada: " + LocalDateTime.now());
            System.out.println("=== FIN DE ACTUALIZACIÓN AUTOMÁTICA ===\n");
        } catch (Exception e) {
            System.err.println("ERROR AL ACTUALIZAR ESTADOS DE MANTENIMIENTOS: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Completa un mantenimiento y actualiza la observación relacionada y el estado de la instalación
     * @param mantenimiento El mantenimiento que se completó
     */
    @Transactional
    private void completarMantenimientoYActualizarObservacion(MantenimientoInstalacion mantenimiento) {
        try {
            System.out.println("=== COMPLETANDO MANTENIMIENTO Y ACTUALIZANDO OBSERVACIÓN ===");
            System.out.println("Mantenimiento ID: " + mantenimiento.getId());
            System.out.println("Instalación ID: " + mantenimiento.getInstalacion().getId());

            // Buscar observaciones en proceso para esta instalación
            List<Observacion> observacionesEnProceso = observacionRepository.findByInstalacionIdAndEstado(
                    mantenimiento.getInstalacion().getId(), "en_proceso");

            System.out.println("Observaciones en proceso encontradas: " + observacionesEnProceso.size());

            // Actualizar todas las observaciones en proceso a "resuelta"
            for (Observacion observacion : observacionesEnProceso) {
                observacion.setEstado("resuelta");
                observacion.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
                observacionRepository.save(observacion);
                System.out.println("Observación ID " + observacion.getId() + " marcada como resuelta");

                // Enviar notificación al coordinador
                try {
                    notificacionService.crearNotificacion(
                            observacion.getUsuario().getId(),
                            "Mantenimiento completado",
                            "El mantenimiento de " + mantenimiento.getInstalacion().getNombre() + " ha sido completado automáticamente.",
                            "mantenimiento"
                    );
                } catch (Exception e) {
                    System.err.println("Error al enviar notificación: " + e.getMessage());
                }
            }

            // Actualizar el estado de la instalación
            Instalacion instalacion = mantenimiento.getInstalacion();
            instalacion.setRequiereMantenimiento(false);
            instalacionRepository.save(instalacion);
            System.out.println("Instalación " + instalacion.getId() + " - requiere_mantenimiento actualizado a false");

            System.out.println("=== MANTENIMIENTO COMPLETADO Y OBSERVACIÓN ACTUALIZADA ===");

        } catch (Exception e) {
            System.err.println("ERROR AL COMPLETAR MANTENIMIENTO Y ACTUALIZAR OBSERVACIÓN: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
