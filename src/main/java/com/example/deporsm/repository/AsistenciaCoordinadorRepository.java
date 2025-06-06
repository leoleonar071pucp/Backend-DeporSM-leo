package com.example.deporsm.repository;

import com.example.deporsm.model.AsistenciaCoordinador;
import com.example.deporsm.model.Instalacion;
import com.example.deporsm.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

public interface AsistenciaCoordinadorRepository extends JpaRepository<AsistenciaCoordinador, Integer> {

    List<AsistenciaCoordinador> findByCoordinador(Usuario coordinador);

    List<AsistenciaCoordinador> findByInstalacion(Instalacion instalacion);

    List<AsistenciaCoordinador> findByFecha(Date fecha);

    List<AsistenciaCoordinador> findByCoordinadorAndFecha(Usuario coordinador, Date fecha);

    List<AsistenciaCoordinador> findByInstalacionAndFecha(Instalacion instalacion, Date fecha);

    List<AsistenciaCoordinador> findByCoordinadorAndInstalacion(Usuario coordinador, Instalacion instalacion);

    /**
     * Consulta para obtener datos de asistencias para reportes
     */
    @Query(value = """
    SELECT
        a.id,
        CONCAT(u.nombre, ' ', u.apellidos) as coordinador,
        i.nombre as instalacion,
        DATE_FORMAT(a.fecha, '%d/%m/%Y') as fecha,
        TIME_FORMAT(a.hora_programada_inicio, '%H:%i') as hora_programada_inicio,
        TIME_FORMAT(a.hora_programada_fin, '%H:%i') as hora_programada_fin,
        TIME_FORMAT(a.hora_entrada, '%H:%i') as hora_entrada,
        a.estado_entrada,
        TIME_FORMAT(a.hora_salida, '%H:%i') as hora_salida,
        a.estado_salida,
        a.ubicacion
    FROM asistencias_coordinadores a
    JOIN usuarios u ON a.coordinador_id = u.id
    JOIN instalaciones i ON a.instalacion_id = i.id
    WHERE DATE(a.fecha) BETWEEN :fechaInicio AND :fechaFin
    AND (:coordinadorNombre IS NULL OR CONCAT(u.nombre, ' ', u.apellidos) LIKE CONCAT('%', :coordinadorNombre, '%'))
    AND (:instalacionNombre IS NULL OR i.nombre LIKE CONCAT('%', :instalacionNombre, '%'))
    AND (:estadoEntrada IS NULL OR a.estado_entrada = :estadoEntrada)
    AND (:estadoSalida IS NULL OR a.estado_salida = :estadoSalida)
    ORDER BY a.id ASC
    """, nativeQuery = true)
    List<Object[]> findAsistenciasForReport(
        @Param("fechaInicio") LocalDate fechaInicio,
        @Param("fechaFin") LocalDate fechaFin,
        @Param("coordinadorNombre") String coordinadorNombre,
        @Param("instalacionNombre") String instalacionNombre,
        @Param("estadoEntrada") String estadoEntrada,
        @Param("estadoSalida") String estadoSalida);

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

    // Verificar si ya existe una asistencia para el coordinador, instalación, fecha y horario específicos
    @Query("SELECT a FROM AsistenciaCoordinador a WHERE a.coordinador.id = :coordinadorId " +
           "AND a.instalacion.id = :instalacionId " +
           "AND a.fecha = :fecha " +
           "AND a.horaProgramadaInicio = :horaProgramadaInicio " +
           "AND a.horaProgramadaFin = :horaProgramadaFin")
    List<AsistenciaCoordinador> findByCoordinadorInstalacionFechaYHorario(
            @Param("coordinadorId") Integer coordinadorId,
            @Param("instalacionId") Integer instalacionId,
            @Param("fecha") Date fecha,
            @Param("horaProgramadaInicio") java.sql.Time horaProgramadaInicio,
            @Param("horaProgramadaFin") java.sql.Time horaProgramadaFin);
}
