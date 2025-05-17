package com.example.deporsm.repository;

import com.example.deporsm.model.HorarioDisponible;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;

@Repository
public interface HorarioDisponibleRepository extends JpaRepository<HorarioDisponible, Integer> {
    List<HorarioDisponible> findByInstalacionId(Integer instalacionId);
    
    List<HorarioDisponible> findByInstalacionIdAndDiaSemana(Integer instalacionId, HorarioDisponible.DiaSemana diaSemana);
    
    @Query("SELECT h FROM HorarioDisponible h WHERE h.instalacion.id = :instalacionId " + 
           "AND h.diaSemana = :diaSemana AND h.disponible = true")
    List<HorarioDisponible> findDisponiblesByInstalacionAndDiaSemana(
            Integer instalacionId, HorarioDisponible.DiaSemana diaSemana);
    
    /**
     * Query to find available time slots for a facility on a specific date
     * This takes into account existing reservations
     */
    @Query(value = 
           "SELECT hd.* FROM horarios_disponibles hd " +
           "WHERE hd.instalacion_id = :instalacionId " +
           "AND hd.dia_semana = :diaSemana " +
           "AND hd.disponible = true " +
           "AND NOT EXISTS ( " +
           "  SELECT 1 FROM reservas r " +
           "  WHERE r.instalacion_id = hd.instalacion_id " +
           "  AND r.fecha = :fecha " +
           "  AND r.estado != 'cancelada' " +
           "  AND ( " +
           "    (r.hora_inicio <= hd.hora_inicio AND r.hora_fin > hd.hora_inicio) OR " +
           "    (r.hora_inicio < hd.hora_fin AND r.hora_fin >= hd.hora_fin) OR " +
           "    (r.hora_inicio >= hd.hora_inicio AND r.hora_fin <= hd.hora_fin) " +
           "  ) " +
           ")",
           nativeQuery = true)
    List<HorarioDisponible> findDisponiblesByFecha(Integer instalacionId, String diaSemana, Date fecha);
}
