package com.example.deporsm.repository;

import com.example.deporsm.dto.MantenimientoDTO;
import com.example.deporsm.model.MantenimientoInstalacion;
import com.example.deporsm.model.Instalacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    }
