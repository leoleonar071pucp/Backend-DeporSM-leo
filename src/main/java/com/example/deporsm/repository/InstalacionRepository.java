package com.example.deporsm.repository;

import com.example.deporsm.dto.InstalacionDTO;
import com.example.deporsm.dto.InstalacionEstadoDTO;
import com.example.deporsm.model.Instalacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface InstalacionRepository extends JpaRepository<Instalacion, Integer> {

    /**
     * Encuentra todas las instalaciones activas
     */
    List<Instalacion> findByActivoTrue();

    List<InstalacionDTO> findByTipo(String tipo);

    List<Instalacion> findByActivo(Boolean activo);

    List<Instalacion> findByNombreContainingIgnoreCase(String nombre);

    List<Instalacion> findByNombreContainingIgnoreCaseAndActivo(String nombre, Boolean activo);

    List<Instalacion> findByTipoAndActivo(String tipo, Boolean activo);

    @Query("SELECT i FROM Instalacion i WHERE LOWER(i.nombre) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(i.tipo) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Instalacion> autocompleteByNombreOrTipo(@Param("query") String query);

    @Query("SELECT i FROM Instalacion i WHERE LOWER(i.ubicacion) LIKE LOWER(CONCAT('%', :ubicacion, '%'))")
    List<Instalacion> buscarPorUbicacion(@Param("ubicacion") String ubicacion);


    @Query(value = """
    SELECT
        i.id AS idInstalacion,
        i.nombre AS nombreInstalacion,
        CASE
            WHEN EXISTS (
                SELECT 1
                FROM mantenimiento_instalaciones m
                WHERE m.instalacion_id = i.id
                  AND CURRENT_TIMESTAMP BETWEEN m.fecha_inicio AND m.fecha_fin
                  AND m.estado NOT IN ('completado', 'cancelado')
            ) THEN 'mantenimiento'
            ELSE 'disponible'
        END AS estado,
        (
            SELECT COUNT(*)
            FROM reservas r
            WHERE r.instalacion_id = i.id
              AND DATE(r.created_at) = CURDATE()
              AND r.estado != 'cancelada'
        ) AS reservasHoy
    FROM instalaciones i
    WHERE i.activo = TRUE
    ORDER BY reservasHoy DESC, i.nombre ASC
""", nativeQuery = true)
    List<InstalacionEstadoDTO> getEstadoActualInstalaciones();

    /**
     * Obtiene las instalaciones más populares basadas en el número total de reservas
     */
    @Query(value = """
    SELECT i.* FROM instalaciones i
    LEFT JOIN (
        SELECT instalacion_id, COUNT(*) as total_reservas
        FROM reservas
        WHERE estado != 'cancelada'
        GROUP BY instalacion_id
    ) r ON i.id = r.instalacion_id
    WHERE i.activo = TRUE
    ORDER BY CASE WHEN r.total_reservas IS NULL THEN 0 ELSE r.total_reservas END DESC, i.nombre ASC
    LIMIT 10
    """, nativeQuery = true)
    List<Instalacion> getInstalacionesPopulares();

    /**
     * Obtiene el número de reservas por instalación para el dashboard
     * Incluye todas las instalaciones y todos los estados de reserva (incluyendo canceladas)
     * Muestra el conteo exacto de reservas tal como aparece en la base de datos
     */
    @Query(value = """
    SELECT
        i.nombre as nombre,
        COUNT(r.id) as total_reservas
    FROM instalaciones i
    LEFT JOIN reservas r ON i.id = r.instalacion_id
    GROUP BY i.id, i.nombre
    ORDER BY total_reservas DESC, i.nombre ASC
    LIMIT 5
    """, nativeQuery = true)
    List<Map<String, Object>> findReservationsByFacility();
}

