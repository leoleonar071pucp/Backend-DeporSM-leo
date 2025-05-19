package com.example.deporsm.repository;

import com.example.deporsm.model.Reporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReporteRepository extends JpaRepository<Reporte, Integer> {

    /**
     * Encuentra reportes por tipo
     */
    List<Reporte> findByTipoOrderByFechaCreacionDesc(String tipo);

    /**
     * Encuentra reportes por formato
     */
    List<Reporte> findByFormatoOrderByFechaCreacionDesc(String formato);

    /**
     * Encuentra reportes por tipo y formato
     */
    List<Reporte> findByTipoAndFormatoOrderByFechaCreacionDesc(String tipo, String formato);

    /**
     * Encuentra reportes por instalación
     */
    List<Reporte> findByInstalacion_IdOrderByFechaCreacionDesc(Integer instalacionId);

    /**
     * Encuentra reportes por usuario
     */
    List<Reporte> findByUsuario_IdOrderByFechaCreacionDesc(Integer usuarioId);

    /**
     * Encuentra los reportes más recientes
     */
    List<Reporte> findTop10ByOrderByFechaCreacionDesc();

    /**
     * Busca reportes por nombre o tipo
     */
    @Query("SELECT r FROM Reporte r WHERE LOWER(r.nombre) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(r.tipo) LIKE LOWER(CONCAT('%', :searchTerm, '%')) ORDER BY r.fechaCreacion DESC")
    List<Reporte> searchByNombreOrTipo(@Param("searchTerm") String searchTerm);
}
