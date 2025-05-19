package com.example.deporsm.repository;

import com.example.deporsm.dto.DashboardStatsDTO;
import com.example.deporsm.dto.ReservaDetalleDTO;
import com.example.deporsm.dto.ReservaRecienteDTO;
import com.example.deporsm.model.Reserva;
import com.example.deporsm.dto.ReservaListDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservaRepository extends JpaRepository<Reserva, Integer> {    // Método para listar reservas para el Admin
    @Query("SELECT new com.example.deporsm.dto.ReservaListDTO(" +
            "r.id, " +
            "u.nombre, " +
            "i.nombre, " +
            "i.ubicacion, " +
            "r.metodoPago, " +
            "i.imagenUrl, " +
            "r.fecha, " +
            "r.horaInicio, " +
            "r.horaFin, " +
            "r.estado, " +
            "r.estadoPago" +
            ") " +
            "FROM Reserva r " +
            "JOIN r.usuario u " +
            "JOIN r.instalacion i " +
            "ORDER BY r.fecha DESC, r.horaInicio DESC")
    List<ReservaListDTO> listarReservasParaAdmin();

    // Método para filtrar reservas por texto y fecha
    @Query("SELECT new com.example.deporsm.dto.ReservaListDTO(" +
            "r.id, " +
            "u.nombre, " +
            "i.nombre, " +
            "i.ubicacion, " +
            "r.metodoPago, " +
            "i.imagenUrl, " +
            "r.fecha, " +
            "r.horaInicio, " +
            "r.horaFin, " +
            "r.estado, " +
            "r.estadoPago" +
            ") " +
            "FROM Reserva r " +
            "JOIN r.usuario u " +
            "JOIN r.instalacion i " +
            "WHERE (:texto IS NULL OR " +
            "LOWER(u.nombre) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
            "LOWER(i.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))) " +
            "AND (:fecha IS NULL OR r.fecha = :fecha) " +
            "ORDER BY r.fecha DESC, r.horaInicio DESC")
    List<ReservaListDTO> filtrarReservas(@Param("texto") String texto, @Param("fecha") java.sql.Date fecha);    // Método para buscar reservas por el DNI del usuario
    @Query("SELECT r FROM Reserva r JOIN r.usuario u WHERE u.dni = :dni")
    List<Reserva> findByUsuario_Dni(@Param("dni") String dni);

    @Query(value = """
    SELECT
      COALESCE(COUNT(*), 0) AS totalReservas,
      COALESCE(SUM(CASE WHEN estado IN ('pendiente', 'confirmada') THEN 1 ELSE 0 END), 0) AS reservasActivas,
      (SELECT COALESCE(COUNT(*), 0) FROM instalaciones WHERE activo = true) AS totalInstalaciones,
      (SELECT COALESCE(COUNT(*), 0) FROM observaciones) AS totalObservaciones
    FROM reservas
    """, nativeQuery = true)
    DashboardStatsDTO getDashboardStats(); // ← preferido si estás usando una proyección basada en constructor

    @Query(value = """
    SELECT
        r.id AS idReserva,
        u.nombre AS nombreUsuario,
        i.nombre AS nombreInstalacion,
        i.id AS instalacionId,
        r.fecha AS fecha,
        r.hora_inicio AS horaInicio,
        r.hora_fin AS horaFin,
        r.estado AS estado,
        r.estado_pago AS estadoPago
    FROM reservas r
    JOIN usuarios u ON r.usuario_id = u.id
    JOIN instalaciones i ON r.instalacion_id = i.id
    WHERE r.estado != 'cancelada'
    ORDER BY r.fecha DESC, r.hora_inicio DESC
    LIMIT 50
    """, nativeQuery = true)
    List<ReservaRecienteDTO> obtenerReservasRecientes();

    // Consulta para obtener los detalles de una reserva específica
    @Query("SELECT new com.example.deporsm.dto.ReservaDetalleDTO(" +
            "r.id, " +
            "u.id, " +
            "u.nombre, " +
            "i.id, " +
            "i.nombre, " +
            "i.ubicacion, " +
            "i.imagenUrl, " +
            "r.fecha, " +
            "r.horaInicio, " +
            "r.horaFin, " +
            "r.estado, " +
            "r.estadoPago, " +
            "r.metodoPago, " +
            "r.comentarios, " +
            "r.createdAt, " +
            "r.updatedAt) " +
            "FROM Reserva r " +
            "JOIN r.usuario u " +
            "JOIN r.instalacion i " +
        "WHERE r.id = :id")
    Optional<ReservaDetalleDTO> obtenerDetalleReserva(@Param("id") Integer id);

    /**
     * Obtiene las reservas recientes para una instalación específica
     * Usamos DATE_FORMAT para asegurar que la fecha se envíe en formato ISO (YYYY-MM-DD)
     */
    @Query(value = """
    SELECT
        r.id AS idReserva,
        u.nombre AS nombreUsuario,
        i.nombre AS nombreInstalacion,
        i.id AS instalacionId,
        DATE_FORMAT(r.fecha, '%Y-%m-%d') AS fecha,
        r.hora_inicio AS horaInicio,
        r.hora_fin AS horaFin,
        r.estado AS estado,
        r.estado_pago AS estadoPago
    FROM reservas r
    JOIN usuarios u ON r.usuario_id = u.id
    JOIN instalaciones i ON r.instalacion_id = i.id
    WHERE r.instalacion_id = :instalacionId
    AND r.estado != 'cancelada'
    ORDER BY r.fecha DESC, r.hora_inicio DESC
    LIMIT 10
    """, nativeQuery = true)
    List<ReservaRecienteDTO> obtenerReservasRecientesPorInstalacion(@Param("instalacionId") Integer instalacionId);

    /**
     * Consulta para obtener datos de reservas para reportes
     */
    @Query(value = """
    SELECT
        r.id,
        CONCAT(u.nombre, ' ', u.apellidos) as usuario,
        i.nombre as instalacion,
        DATE_FORMAT(r.fecha, '%d/%m/%Y') as fecha,
        TIME_FORMAT(r.hora_inicio, '%H:%i') as hora_inicio,
        TIME_FORMAT(r.hora_fin, '%H:%i') as hora_fin,
        r.estado,
        r.estado_pago,
        r.metodo_pago
    FROM reservas r
    JOIN usuarios u ON r.usuario_id = u.id
    JOIN instalaciones i ON r.instalacion_id = i.id
    WHERE r.fecha BETWEEN :fechaInicio AND :fechaFin
    AND (:instalacionId IS NULL OR r.instalacion_id = :instalacionId)
    ORDER BY r.fecha DESC, r.hora_inicio DESC
    """, nativeQuery = true)
    List<Object[]> findReservasForReport(
        @Param("fechaInicio") LocalDate fechaInicio,
        @Param("fechaFin") LocalDate fechaFin,
        @Param("instalacionId") Integer instalacionId);

    /**
     * Consulta para obtener datos de ingresos para reportes
     */
    @Query(value = """
    SELECT
        DATE_FORMAT(fecha_agrupada, '%d/%m/%Y') as fecha,
        instalacion,
        total_reservas,
        total_ingresos
    FROM (
        SELECT
            DATE(r.fecha) as fecha_agrupada,
            i.nombre as instalacion,
            COUNT(r.id) as total_reservas,
            SUM(TIMESTAMPDIFF(MINUTE, r.hora_inicio, r.hora_fin) * i.precio / 60) as total_ingresos
        FROM reservas r
        JOIN instalaciones i ON r.instalacion_id = i.id
        WHERE r.fecha BETWEEN :fechaInicio AND :fechaFin
        AND r.estado_pago = 'pagado'
        AND (:instalacionId IS NULL OR r.instalacion_id = :instalacionId)
        GROUP BY DATE(r.fecha), i.id, i.nombre
    ) AS subquery
    ORDER BY fecha_agrupada DESC
    """, nativeQuery = true)
    List<Object[]> findIngresosForReport(
        @Param("fechaInicio") LocalDate fechaInicio,
        @Param("fechaFin") LocalDate fechaFin,
        @Param("instalacionId") Integer instalacionId);

    /**
     * Consulta para obtener datos de uso de instalaciones para reportes
     */
    @Query(value = """
    SELECT
        i.nombre as instalacion,
        COUNT(r.id) as total_reservas,
        SUM(TIMESTAMPDIFF(HOUR, r.hora_inicio, r.hora_fin)) as horas_reservadas,
        ROUND(
            CASE
                WHEN (SELECT SUM(TIMESTAMPDIFF(MINUTE, hd.hora_inicio, hd.hora_fin))
                     FROM horarios_disponibles hd
                     WHERE hd.instalacion_id = i.id) > 0
                THEN
                    (COALESCE(SUM(TIMESTAMPDIFF(MINUTE, r.hora_inicio, r.hora_fin)), 0) /
                    (DATEDIFF(:fechaFin, :fechaInicio) + 1) /
                    (SELECT SUM(TIMESTAMPDIFF(MINUTE, hd.hora_inicio, hd.hora_fin))
                     FROM horarios_disponibles hd
                     WHERE hd.instalacion_id = i.id) * 100)
                ELSE 0
            END, 2) as porcentaje_ocupacion,
        (SELECT horario FROM (
            SELECT
                CONCAT(TIME_FORMAT(r2.hora_inicio, '%H:%i'), ' - ', TIME_FORMAT(r2.hora_fin, '%H:%i')) as horario,
                COUNT(*) as total
            FROM reservas r2
            WHERE r2.instalacion_id = i.id
            AND r2.fecha BETWEEN :fechaInicio AND :fechaFin
            GROUP BY horario
            ORDER BY total DESC
            LIMIT 1
         ) as subquery) as horario_mas_popular
    FROM instalaciones i
    LEFT JOIN reservas r ON i.id = r.instalacion_id AND r.fecha BETWEEN :fechaInicio AND :fechaFin
    WHERE (:instalacionId IS NULL OR i.id = :instalacionId)
    GROUP BY i.id, i.nombre
    ORDER BY total_reservas DESC
    """, nativeQuery = true)
    List<Object[]> findInstalacionesUsageForReport(
        @Param("fechaInicio") LocalDate fechaInicio,
        @Param("fechaFin") LocalDate fechaFin,
        @Param("instalacionId") Integer instalacionId);
}
