package com.example.deporsm.repository;

import com.example.deporsm.model.LogActividad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LogActividadRepository extends JpaRepository<LogActividad, Integer> {

    List<LogActividad> findTop100ByOrderByCreatedAtDesc();

    @Query(value = """
        SELECT l.* FROM log_actividades l
        LEFT JOIN usuarios u ON l.usuario_id = u.id
        LEFT JOIN roles r ON u.role_id = r.id
        WHERE 
            (:role IS NULL OR r.nombre = :role)
            AND (:action IS NULL OR l.accion = :action)
            AND (:status IS NULL OR l.estado = :status)
            AND (:startDate IS NULL OR l.created_at >= :startDate)
            AND (:endDate IS NULL OR l.created_at <= :endDate)
        ORDER BY l.created_at DESC
        LIMIT 100
    """, nativeQuery = true)
    List<LogActividad> findByFilters(
        @Param("role") String role,
        @Param("action") String action,
        @Param("status") String status,
        @Param("startDate") String startDate,
        @Param("endDate") String endDate
    );
}
