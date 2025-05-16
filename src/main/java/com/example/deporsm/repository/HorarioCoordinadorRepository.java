package com.example.deporsm.repository;

import com.example.deporsm.model.HorarioCoordinador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HorarioCoordinadorRepository extends JpaRepository<HorarioCoordinador, Integer> {
    
    List<HorarioCoordinador> findByCoordinadorInstalacionId(Integer coordinadorInstalacionId);
      // Versión simplificada sin usar FIELD() que podría causar problemas
    @Query(value = 
           "SELECT hc.* FROM horarios_coordinadores hc " +
           "JOIN coordinadores_instalaciones ci ON hc.coordinador_instalacion_id = ci.id " +
           "WHERE ci.usuario_id = :usuarioId " +
           "ORDER BY CASE hc.dia_semana " +
           "WHEN 'lunes' THEN 1 " +
           "WHEN 'martes' THEN 2 " +
           "WHEN 'miercoles' THEN 3 " +
           "WHEN 'jueves' THEN 4 " +
           "WHEN 'viernes' THEN 5 " +
           "WHEN 'sabado' THEN 6 " +
           "WHEN 'domingo' THEN 7 " +
           "ELSE 8 END, " +
           "hc.hora_inicio",
           nativeQuery = true)
    List<HorarioCoordinador> findHorariosCoordinadorByUsuarioId(Integer usuarioId);
      // Versión simplificada sin usar FIELD() que podría causar problemas
    @Query(value = 
           "SELECT hc.* FROM horarios_coordinadores hc " +
           "JOIN coordinadores_instalaciones ci ON hc.coordinador_instalacion_id = ci.id " +
           "WHERE ci.usuario_id = :usuarioId AND ci.instalacion_id = :instalacionId " +
           "ORDER BY CASE hc.dia_semana " +
           "WHEN 'lunes' THEN 1 " +
           "WHEN 'martes' THEN 2 " +
           "WHEN 'miercoles' THEN 3 " +
           "WHEN 'jueves' THEN 4 " +
           "WHEN 'viernes' THEN 5 " +
           "WHEN 'sabado' THEN 6 " +
           "WHEN 'domingo' THEN 7 " +
           "ELSE 8 END, " +
           "hc.hora_inicio",
           nativeQuery = true)
    List<HorarioCoordinador> findHorariosCoordinadorByUsuarioIdAndInstalacionId(Integer usuarioId, Integer instalacionId);
}
