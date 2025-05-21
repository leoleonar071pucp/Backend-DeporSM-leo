// src/main/java/com/example/deporsm/repository/ObservacionRepository.java
package com.example.deporsm.repository;

import com.example.deporsm.dto.ObservacionInstalacionDTO;
import com.example.deporsm.dto.ObservacionRecienteDTO;
import com.example.deporsm.model.Observacion;
import com.example.deporsm.dto.ObservacionDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ObservacionRepository extends JpaRepository<Observacion, Integer> {

    @Query(value = """
        SELECT
            i.nombre as instalacion,
            COUNT(o.id) as cantidad
        FROM observaciones o
        JOIN instalaciones i ON o.instalacion_id = i.id
        GROUP BY i.nombre
        ORDER BY cantidad DESC
        LIMIT 5
    """, nativeQuery = true)
    List<Map<String, Object>> findObservacionesPorInstalacion();

    @Query(value = """
        SELECT
            m.estado as estado,
            COUNT(m.id) as cantidad
        FROM mantenimiento_instalaciones m
        GROUP BY m.estado
        ORDER BY cantidad DESC
    """, nativeQuery = true)
    List<Map<String, Object>> findEstadoMantenimientos();    @Query(value = """
        SELECT
            o.id as idObservacion,
            i.nombre AS instalacion,
            o.titulo AS titulo,
            o.descripcion AS descripcion,
            CONCAT(u.nombre, ' ', u.apellidos) AS coordinador,
            DATE_FORMAT(o.created_at, '%d/%m/%Y') AS fecha,
            o.estado AS estado,
            o.prioridad AS prioridad,
            i.ubicacion AS ubicacion,
            o.fotos_url AS fotosUrl
        FROM
            deportes_sm.observaciones o
        INNER JOIN
            deportes_sm.instalaciones i ON o.instalacion_id = i.id
        INNER JOIN
            deportes_sm.usuarios u ON o.usuario_id = u.id
        ORDER BY
            o.created_at DESC
        """, nativeQuery = true)
List<Object[]> findAllObservacionesRaw();    default List<ObservacionDTO> findAllObservacionesDTO() {
    return findAllObservacionesRaw().stream()
            .map(row -> new ObservacionDTO(
                    (Integer) row[0],
                    (String) row[1],
                    (String) row[2],
                    (String) row[3],
                    (String) row[4],
                    (String) row[5],
                    (String) row[6],
                    (String) row[7],
                    (String) row[8],
                    (String) row[9]
            ))
            .toList();
}@Query(value = """
        SELECT
            o.id AS idObservacion,
            i.nombre AS nombreInstalacion,
            o.descripcion,
            o.prioridad,
            DATE(o.created_at) AS fecha
        FROM observaciones o
        JOIN instalaciones i ON o.instalacion_id = i.id
        ORDER BY o.created_at DESC
        LIMIT 4
    """, nativeQuery = true)
List<ObservacionRecienteDTO> findObservacionesRecientes();      @Query(value = """
        SELECT
            o.id as idObservacion,
            i.nombre AS instalacion,
            o.titulo AS titulo,
            o.descripcion AS descripcion,
            CONCAT(u.nombre, ' ', u.apellidos) AS coordinador,
            DATE_FORMAT(o.created_at, '%d/%m/%Y') AS fecha,
            o.estado AS estado,
            o.prioridad AS prioridad,
            i.ubicacion AS ubicacion,
            o.fotos_url AS fotosUrl
        FROM
            observaciones o
        INNER JOIN
            instalaciones i ON o.instalacion_id = i.id
        INNER JOIN
            usuarios u ON o.usuario_id = u.id
        INNER JOIN
            coordinadores_instalaciones ci ON ci.instalacion_id = i.id
        WHERE
            ci.usuario_id = :usuarioId
        ORDER BY
            o.created_at DESC
    """, nativeQuery = true)
List<Object[]> findObservacionesByCoordinadorId(Integer usuarioId);
    default List<ObservacionDTO> findObservacionesDTOByCoordinadorId(Integer usuarioId) {
        return findObservacionesByCoordinadorId(usuarioId).stream()
                .map(row -> new ObservacionDTO(
                        (Integer) row[0],
                        (String) row[1],
                        (String) row[2],
                        (String) row[3],
                        (String) row[4],
                        (String) row[5],
                        (String) row[6],
                        (String) row[7],
                        (String) row[8],
                        (String) row[9]
                ))
                .toList();
    }

    /**
     * Busca observaciones por instalación y estado
     * @param instalacionId ID de la instalación
     * @param estado Estado de la observación
     * @return Lista de observaciones que cumplen con los criterios
     */
    List<Observacion> findByInstalacionIdAndEstado(Integer instalacionId, String estado);

    /**
     * Obtiene las observaciones recientes para una instalación específica
     * @param instalacionId ID de la instalación
     * @return Lista de observaciones recientes para la instalación
     */
    @Query(value = """
        SELECT
            o.id AS idObservacion,
            i.nombre AS nombreInstalacion,
            o.titulo AS titulo,
            o.descripcion,
            o.prioridad,
            DATE_FORMAT(o.created_at, '%Y-%m-%d') AS fecha,
            o.estado AS estado,
            CONCAT(u.nombre, ' ', u.apellidos) AS coordinador
        FROM observaciones o
        JOIN instalaciones i ON o.instalacion_id = i.id
        JOIN usuarios u ON o.usuario_id = u.id
        WHERE o.instalacion_id = :instalacionId
        ORDER BY o.created_at DESC
        LIMIT 5
    """, nativeQuery = true)
    List<ObservacionInstalacionDTO> findObservacionesRecientesPorInstalacion(@org.springframework.data.repository.query.Param("instalacionId") Integer instalacionId);
}