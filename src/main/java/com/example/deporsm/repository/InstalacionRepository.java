package com.example.deporsm.repository;

import com.example.deporsm.dto.InstalacionDTO;
import com.example.deporsm.dto.InstalacionEstadoDTO;
import com.example.deporsm.model.Instalacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InstalacionRepository extends JpaRepository<Instalacion, Integer> {

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
        i.id AS id_instalacion,
        i.nombre AS nombre_instalacion,
        CASE 
            WHEN EXISTS (
                SELECT 1 
                FROM mantenimiento_instalaciones m 
                WHERE m.instalacion_id = i.id
                  AND CURRENT_TIMESTAMP BETWEEN m.fecha_inicio AND m.fecha_fin
            ) THEN 'mantenimiento'
            ELSE 'disponible'
        END AS estado,
        (
            SELECT COUNT(*) 
            FROM reservas r 
            WHERE r.instalacion_id = i.id 
              AND DATE(r.fecha) = CURDATE()
        ) AS reservas_hoy
    FROM instalaciones i
    WHERE i.activo = TRUE
    ORDER BY reservas_hoy DESC, i.nombre ASC
    LIMIT 7
""", nativeQuery = true)
    List<InstalacionEstadoDTO> getEstadoActualInstalaciones();


}

