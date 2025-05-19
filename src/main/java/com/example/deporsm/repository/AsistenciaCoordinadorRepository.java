package com.example.deporsm.repository;

import com.example.deporsm.model.AsistenciaCoordinador;
import com.example.deporsm.model.Instalacion;
import com.example.deporsm.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Date;
import java.util.List;

public interface AsistenciaCoordinadorRepository extends JpaRepository<AsistenciaCoordinador, Integer> {
    
    List<AsistenciaCoordinador> findByCoordinador(Usuario coordinador);
    
    List<AsistenciaCoordinador> findByInstalacion(Instalacion instalacion);
    
    List<AsistenciaCoordinador> findByFecha(Date fecha);
    
    List<AsistenciaCoordinador> findByCoordinadorAndFecha(Usuario coordinador, Date fecha);
    
    List<AsistenciaCoordinador> findByInstalacionAndFecha(Instalacion instalacion, Date fecha);
    
    List<AsistenciaCoordinador> findByCoordinadorAndInstalacion(Usuario coordinador, Instalacion instalacion);
    
    @Query("SELECT a FROM AsistenciaCoordinador a WHERE a.coordinador.id = :coordinadorId AND a.fecha BETWEEN :fechaInicio AND :fechaFin")
    List<AsistenciaCoordinador> findByCoordinadorIdAndFechaBetween(
            @Param("coordinadorId") Integer coordinadorId,
            @Param("fechaInicio") Date fechaInicio,
            @Param("fechaFin") Date fechaFin);
            
    @Query("SELECT a FROM AsistenciaCoordinador a WHERE a.instalacion.id = :instalacionId AND a.fecha BETWEEN :fechaInicio AND :fechaFin")
    List<AsistenciaCoordinador> findByInstalacionIdAndFechaBetween(
            @Param("instalacionId") Integer instalacionId,
            @Param("fechaInicio") Date fechaInicio,
            @Param("fechaFin") Date fechaFin);
            
    @Query("SELECT a FROM AsistenciaCoordinador a WHERE a.estadoEntrada = :estado OR a.estadoSalida = :estado")
    List<AsistenciaCoordinador> findByEstadoEntradaOrEstadoSalida(
            @Param("estado") AsistenciaCoordinador.EstadoAsistencia estado);
}
