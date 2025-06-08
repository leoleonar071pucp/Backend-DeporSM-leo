package com.example.deporsm.service;

import com.example.deporsm.dto.BloqueoTemporalDTO;
import com.example.deporsm.model.BloqueoTemporal;
import com.example.deporsm.model.Instalacion;
import com.example.deporsm.model.MantenimientoInstalacion;
import com.example.deporsm.model.Usuario;
import com.example.deporsm.repository.BloqueoTemporalRepository;
import com.example.deporsm.repository.InstalacionRepository;
import com.example.deporsm.repository.MantenimientoInstalacionRepository;
import com.example.deporsm.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;

@Service
public class BloqueoTemporalService {

    @Autowired
    private BloqueoTemporalRepository bloqueoTemporalRepository;

    @Autowired
    private InstalacionRepository instalacionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private MantenimientoInstalacionRepository mantenimientoRepository;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Crea un bloqueo temporal para un horario específico
     * @param email Email del usuario
     * @param bloqueoDTO Datos del bloqueo
     * @return DTO con los datos del bloqueo creado, incluyendo el token
     * @throws RuntimeException si no se encuentra el usuario o la instalación,
     *         o si el horario ya está bloqueado
     */
    @Transactional
    public BloqueoTemporalDTO crearBloqueoTemporal(String email, BloqueoTemporalDTO bloqueoDTO) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Instalacion instalacion = instalacionRepository.findById(bloqueoDTO.getInstalacionId())
                .orElseThrow(() -> new RuntimeException("Instalación no encontrada"));

        if (!instalacion.getActivo()) {
            throw new RuntimeException("La instalación no está disponible para reservas");
        }

        // Convertir strings a tipos SQL
        Date fecha = convertirStringADate(bloqueoDTO.getFecha());
        Time horaInicio = convertirStringATime(bloqueoDTO.getHoraInicio());
        Time horaFin = convertirStringATime(bloqueoDTO.getHoraFin());

        // Verificar si el horario está disponible (no reservado y no bloqueado temporalmente)
        if (!verificarDisponibilidadHorario(
                instalacion.getId(),
                fecha,
                horaInicio,
                horaFin)) {
            throw new RuntimeException("El horario seleccionado no está disponible");
        }

        // Crear el bloqueo temporal
        BloqueoTemporal bloqueo = new BloqueoTemporal(
                instalacion,
                usuario,
                fecha,
                horaInicio,
                horaFin);

        bloqueo = bloqueoTemporalRepository.save(bloqueo);

        // Devolver el DTO con el token (convertir de vuelta a strings)
        BloqueoTemporalDTO responseDTO = new BloqueoTemporalDTO(
                bloqueo.getInstalacion().getId(),
                bloqueo.getFecha().toString(),
                bloqueo.getHoraInicio().toString(),
                bloqueo.getHoraFin().toString(),
                bloqueo.getToken());

        return responseDTO;
    }

    /**
     * Verifica si un horario está disponible (no reservado, no bloqueado temporalmente y no afectado por mantenimiento)
     */
    public boolean verificarDisponibilidadHorario(Integer instalacionId, Date fecha,
                                                Time horaInicio, Time horaFin) {
        try {
            // Verificar si hay reservas existentes que se solapen con el horario solicitado
            String jpqlReservas = "SELECT COUNT(r) FROM Reserva r " +
                        "WHERE r.instalacion.id = :instalacionId " +
                        "AND r.fecha = :fecha " +
                        "AND r.estado != 'cancelada' " +
                        "AND ((r.horaInicio <= :horaInicio AND r.horaFin > :horaInicio) " +
                        "OR (r.horaInicio < :horaFin AND r.horaFin >= :horaFin) " +
                        "OR (r.horaInicio >= :horaInicio AND r.horaFin <= :horaFin))";

            Long countReservas = entityManager.createQuery(jpqlReservas, Long.class)
                    .setParameter("instalacionId", instalacionId)
                    .setParameter("fecha", fecha)
                    .setParameter("horaInicio", horaInicio)
                    .setParameter("horaFin", horaFin)
                    .getSingleResult();

            if (countReservas > 0) {
                System.out.println("Horario no disponible: Ya hay reservas para este horario");
                return false; // Ya hay reservas para este horario
            }

            // Verificar si hay bloqueos temporales activos que se solapen con el horario solicitado
            Timestamp ahora = new Timestamp(System.currentTimeMillis());
            List<BloqueoTemporal> bloqueos = bloqueoTemporalRepository.findActiveBlocksForTimeSlot(
                    instalacionId, fecha, horaInicio, horaFin, ahora);

            if (!bloqueos.isEmpty()) {
                System.out.println("Horario no disponible: Hay " + bloqueos.size() + " bloqueos temporales activos");
                return false; // Hay bloqueos temporales activos
            }

            // Imprimir información detallada de la fecha y hora que se está verificando
            System.out.println("=== VERIFICANDO DISPONIBILIDAD PARA HORARIO ===");
            System.out.println("Instalación ID: " + instalacionId);
            System.out.println("Fecha solicitada: " + fecha + " (Clase: " + fecha.getClass().getName() + ")");
            System.out.println("Hora inicio: " + horaInicio + " (Clase: " + horaInicio.getClass().getName() + ")");
            System.out.println("Hora fin: " + horaFin + " (Clase: " + horaFin.getClass().getName() + ")");
            System.out.println("Fecha como LocalDate: " + fecha.toLocalDate());
            System.out.println("Hora inicio como LocalTime: " + horaInicio.toLocalTime());
            System.out.println("Hora fin como LocalTime: " + horaFin.toLocalTime());
            System.out.println("===========================================");

            // Verificar si hay mantenimientos programados o en progreso que afecten la disponibilidad
            List<MantenimientoInstalacion> mantenimientos = mantenimientoRepository.findMantenimientosQueAfectanHorario(
                    instalacionId, fecha, horaInicio, horaFin);

            if (!mantenimientos.isEmpty()) {
                System.out.println("Horario no disponible: Hay " + mantenimientos.size() +
                                  " mantenimientos que afectan la disponibilidad");

                // Imprimir información detallada de los mantenimientos
                System.out.println("=== MANTENIMIENTOS QUE AFECTAN HORARIO ===");
                System.out.println("Instalación ID: " + instalacionId);
                System.out.println("Fecha solicitada: " + fecha);
                System.out.println("Horario solicitado: " + horaInicio + " - " + horaFin);

                for (MantenimientoInstalacion m : mantenimientos) {
                    System.out.println("Mantenimiento ID: " + m.getId() +
                                      ", Inicio: " + m.getFechaInicio() +
                                      ", Fin: " + m.getFechaFin() +
                                      ", Estado: " + m.getEstado());
                }

                System.out.println("=========================================");

                return false; // Hay mantenimientos que afectan la disponibilidad
            }

            System.out.println("Horario disponible: No hay reservas, bloqueos temporales ni mantenimientos");
            return true; // Si no hay reservas, bloqueos ni mantenimientos, el horario está disponible
        } catch (Exception e) {
            System.err.println("Error al verificar disponibilidad de horario: " + e.getMessage());
            e.printStackTrace();
            // En caso de error, asumimos que el horario está disponible para no bloquear la funcionalidad
            return true;
        }
    }

    /**
     * Libera un bloqueo temporal usando su token
     */
    @Transactional
    public void liberarBloqueo(String token) {
        bloqueoTemporalRepository.findByToken(token)
                .ifPresent(bloqueo -> bloqueoTemporalRepository.delete(bloqueo));
    }

    /**
     * Libera todos los bloqueos temporales de un usuario
     */
    @Transactional
    public void liberarBloqueosPorUsuario(Integer usuarioId) {
        bloqueoTemporalRepository.deleteByUsuarioId(usuarioId);
    }

    /**
     * Libera bloqueos temporales para un horario específico de una instalación
     * Útil cuando se cancela una reserva para liberar el horario inmediatamente
     */
    @Transactional
    public void liberarBloqueosPorHorario(Integer instalacionId, Date fecha, Time horaInicio, Time horaFin) {
        try {
            Timestamp ahora = new Timestamp(System.currentTimeMillis());
            List<BloqueoTemporal> bloqueos = bloqueoTemporalRepository.findActiveBlocksForTimeSlot(
                    instalacionId, fecha, horaInicio, horaFin, ahora);

            for (BloqueoTemporal bloqueo : bloqueos) {
                bloqueoTemporalRepository.delete(bloqueo);
                System.out.println("Bloqueo temporal liberado para horario específico. Token: " + bloqueo.getToken());
            }

            System.out.println("Se liberaron " + bloqueos.size() + " bloqueos temporales para el horario especificado");
        } catch (Exception e) {
            System.err.println("Error al liberar bloqueos temporales por horario: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Tarea programada para eliminar bloqueos expirados cada minuto
     */
    @Scheduled(fixedRate = 60000) // Ejecutar cada minuto
    @Transactional
    public void limpiarBloqueosExpirados() {
        try {
            Timestamp ahora = new Timestamp(System.currentTimeMillis());
            bloqueoTemporalRepository.deleteExpiredBlocks(ahora);
        } catch (Exception e) {
            // Si la tabla no existe, registrar el error pero no interrumpir la aplicación
            System.err.println("Error al limpiar bloqueos expirados: " + e.getMessage());
            // No propagar la excepción para evitar que la tarea programada falle
        }
    }

    /**
     * Convierte un string de fecha (YYYY-MM-DD) a java.sql.Date
     * Evita problemas de zona horaria al usar Date.valueOf directamente
     */
    private Date convertirStringADate(String fechaStr) {
        if (fechaStr == null || fechaStr.trim().isEmpty()) {
            return null;
        }
        try {
            // Date.valueOf espera formato YYYY-MM-DD y no aplica conversiones de zona horaria
            return Date.valueOf(fechaStr);
        } catch (Exception e) {
            System.err.println("[ERROR] Error al convertir fecha: " + fechaStr + " - " + e.getMessage());
            throw new RuntimeException("Formato de fecha inválido: " + fechaStr + ". Use formato YYYY-MM-DD");
        }
    }

    /**
     * Convierte un string de tiempo (HH:MM o HH:MM:SS) a java.sql.Time
     */
    private Time convertirStringATime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return null;
        }
        try {
            // Si no tiene segundos, agregarlos
            if (timeStr.length() == 5) {
                timeStr += ":00";
            }
            return Time.valueOf(timeStr);
        } catch (Exception e) {
            System.err.println("[ERROR] Error al convertir tiempo: " + timeStr + " - " + e.getMessage());
            throw new RuntimeException("Formato de tiempo inválido: " + timeStr + ". Use formato HH:MM o HH:MM:SS");
        }
    }
}
