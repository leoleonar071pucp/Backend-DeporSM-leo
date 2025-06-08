package com.example.deporsm.repository;

import com.example.deporsm.dto.MantenimientoDTO;
import com.example.deporsm.model.MantenimientoInstalacion;
import com.example.deporsm.model.Instalacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface MantenimientoInstalacionRepository extends JpaRepository<MantenimientoInstalacion, Integer> {

    // --- Buscar por texto en motivo o descripción ---
    @Query("SELECT m FROM MantenimientoInstalacion m " +
            "WHERE LOWER(m.descripcion) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(m.motivo) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<MantenimientoInstalacion> buscarPorTexto(@Param("query") String query);

    // --- Filtrar por instalación ---
    List<MantenimientoInstalacion> findByInstalacion(Instalacion instalacion);

    // --- Filtrar por instalación y estado ---
    @Query("SELECT m FROM MantenimientoInstalacion m " +
           "WHERE m.instalacion.id = :instalacionId AND m.estado = :estado")
    List<MantenimientoInstalacion> findByInstalacionIdAndEstado(
            @Param("instalacionId") Integer instalacionId,
            @Param("estado") String estado);

    // --- Buscar mantenimientos por instalación y estado, ordenados por fecha ---
    @Query("SELECT m FROM MantenimientoInstalacion m " +
           "WHERE m.instalacion.id = :instalacionId AND m.estado = :estado " +
           "ORDER BY m.fechaFin DESC")
    List<MantenimientoInstalacion> findByInstalacionIdAndEstadoOrderByFechaFinDesc(
            @Param("instalacionId") Integer instalacionId,
            @Param("estado") String estado);

    @Query("SELECT m FROM MantenimientoInstalacion m " +
           "WHERE m.instalacion.id = :instalacionId AND m.estado = :estado " +
           "ORDER BY m.fechaInicio ASC")
    List<MantenimientoInstalacion> findByInstalacionIdAndEstadoOrderByFechaInicioAsc(
            @Param("instalacionId") Integer instalacionId,
            @Param("estado") String estado);

    // --- Segmentación por tiempo ---
    @Query("SELECT m FROM MantenimientoInstalacion m WHERE " +
           "m.estado = 'en-progreso'")
    List<MantenimientoInstalacion> findActivos(@Param("fechaActual") LocalDateTime fechaActual);

    @Query("SELECT m FROM MantenimientoInstalacion m WHERE " +
           "m.estado = 'programado'")
    List<MantenimientoInstalacion> findProgramados(@Param("fechaActual") LocalDateTime fechaActual);

    @Query("SELECT m FROM MantenimientoInstalacion m WHERE " +
           "m.estado = 'completado' OR m.estado = 'cancelado'")
    List<MantenimientoInstalacion> findFinalizados(@Param("fechaActual") LocalDateTime fechaActual);

    /**
     * Busca mantenimientos activos (programados o en progreso) para una instalación
     */
    @Query("SELECT m FROM MantenimientoInstalacion m " +
           "WHERE m.instalacion.id = :instalacionId " +
           "AND (m.estado = 'programado' OR m.estado = 'en-progreso') " +
           "ORDER BY m.fechaInicio ASC")
    List<MantenimientoInstalacion> findMantenimientosActivos(@Param("instalacionId") Integer instalacionId);

    // --- Filtro múltiple general por texto, tipo, estado e instalación ---
    @Query("""

            SELECT new com.example.deporsm.dto.MantenimientoDTO(
        m.id,
        m.motivo,
        m.tipo,
        m.descripcion,
        m.fechaInicio,
        m.fechaFin,
        m.estado,
        m.instalacion.nombre,
        m.instalacion.ubicacion
    )
    FROM MantenimientoInstalacion m
    WHERE (:texto IS NULL OR LOWER(m.descripcion) LIKE LOWER(CONCAT('%', :texto, '%'))
           OR LOWER(m.motivo) LIKE LOWER(CONCAT('%', :texto, '%')))
      AND (:instalacionId IS NULL OR m.instalacion.id = :instalacionId)
    """)
    List<MantenimientoDTO> filtrarPorCriterios(@Param("texto") String texto,
                                               @Param("instalacionId") Integer instalacionId);

    /**
     * Consulta para obtener datos de mantenimientos para reportes
     * Filtra por fecha de inicio del mantenimiento para mostrar mantenimientos en el período seleccionado
     */
    @Query(value = """
    SELECT
        m.id,
        i.nombre as instalacion,
        m.tipo,
        m.descripcion,
        DATE_FORMAT(m.fecha_inicio, '%d/%m/%Y %H:%i') as fecha_inicio,
        DATE_FORMAT(m.fecha_fin, '%d/%m/%Y %H:%i') as fecha_fin,
        m.estado,
        CASE WHEN m.afecta_disponibilidad = 1 THEN 'Sí' ELSE 'No' END as afecta_disponibilidad
    FROM mantenimiento_instalaciones m
    JOIN instalaciones i ON m.instalacion_id = i.id
    WHERE DATE(m.fecha_inicio) BETWEEN :fechaInicio AND :fechaFin
    AND (:instalacionId IS NULL OR m.instalacion_id = :instalacionId)
    ORDER BY m.id ASC
    """, nativeQuery = true)
    List<Object[]> findMantenimientosForReport(
        @Param("fechaInicio") LocalDate fechaInicio,
        @Param("fechaFin") LocalDate fechaFin,
        @Param("instalacionId") Integer instalacionId);

    /**
     * Busca mantenimientos activos (programados o en progreso) que afectan la disponibilidad
     * y se solapan con un horario específico
     *
     * La consulta verifica cuatro casos de solapamiento:
     * 1. Mantenimiento que comienza y termina en el mismo día que el horario solicitado:
     *    - Verifica si hay solapamiento entre el horario y el período de mantenimiento
     *
     * 2. Mantenimiento que comienza en el día del horario solicitado y termina en un día posterior:
     *    - Bloquea cualquier horario cuya hora de fin sea posterior a la hora de inicio del mantenimiento
     *    - Esto garantiza que se bloqueen todos los horarios que se solapan con el mantenimiento
     *
     * 3. Mantenimiento que comenzó en un día anterior y termina en el día del horario solicitado:
     *    - Bloquea cualquier horario cuya hora de inicio sea anterior a la hora de fin del mantenimiento
     *
     * 4. Mantenimiento que abarca completamente el día del horario solicitado:
     *    - Bloquea todos los horarios de ese día
     *
     * @param instalacionId ID de la instalación
     * @param fecha Fecha en formato SQL Date
     * @param horaInicio Hora de inicio en formato SQL Time
     * @param horaFin Hora de fin en formato SQL Time
     * @return Lista de mantenimientos que se solapan con el horario especificado
     */
    @Query("""
    SELECT m FROM MantenimientoInstalacion m
    WHERE m.instalacion.id = :instalacionId
    AND (m.estado = 'programado' OR m.estado = 'en-progreso')
    AND m.afectaDisponibilidad = true
    AND (
        (CAST(:fecha AS date) = CAST(m.fechaInicio AS date) AND CAST(:fecha AS date) = CAST(m.fechaFin AS date)
         AND (
            (CAST(m.fechaInicio AS time) <= :horaInicio AND CAST(m.fechaFin AS time) > :horaInicio) OR
            (CAST(m.fechaInicio AS time) < :horaFin AND CAST(m.fechaFin AS time) >= :horaFin) OR
            (CAST(m.fechaInicio AS time) >= :horaInicio AND CAST(m.fechaFin AS time) <= :horaFin)
         ))
        OR
        (CAST(:fecha AS date) = CAST(m.fechaInicio AS date) AND CAST(:fecha AS date) < CAST(m.fechaFin AS date)
         AND :horaFin > CAST(m.fechaInicio AS time))
        OR
        (CAST(:fecha AS date) > CAST(m.fechaInicio AS date) AND CAST(:fecha AS date) = CAST(m.fechaFin AS date)
         AND CAST(m.fechaFin AS time) > :horaInicio)
        OR
        (CAST(:fecha AS date) > CAST(m.fechaInicio AS date) AND CAST(:fecha AS date) < CAST(m.fechaFin AS date))
    )
    """)
    List<MantenimientoInstalacion> findMantenimientosQueAfectanHorario(
        @Param("instalacionId") Integer instalacionId,
        @Param("fecha") java.sql.Date fecha,
        @Param("horaInicio") java.sql.Time horaInicio,
        @Param("horaFin") java.sql.Time horaFin);
}
