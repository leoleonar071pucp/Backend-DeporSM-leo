package com.example.deporsm.repository;

import com.example.deporsm.dto.DashboardStatsDTO;
import com.example.deporsm.dto.ReservaDetalleDTO;
import com.example.deporsm.dto.ReservaRecienteDTO;
import com.example.deporsm.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ReservaRepository extends JpaRepository<Reserva, Integer> {    // Método para listar reservas para el Admin
    @Query(value = """
    SELECT
        r.id,
        CONCAT(u.nombre, ' ', u.apellidos) AS usuarioNombre,
        i.nombre AS instalacionNombre,
        i.ubicacion AS instalacionUbicacion,
        r.metodo_pago AS metodoPago,
        i.imagen_url AS instalacionImagenUrl,
        r.fecha,
        r.hora_inicio AS horaInicio,
        r.hora_fin AS horaFin,
        r.estado,
        r.estado_pago AS estadoPago
    FROM reservas r
    JOIN usuarios u ON r.usuario_id = u.id
    JOIN instalaciones i ON r.instalacion_id = i.id
    ORDER BY r.fecha DESC, r.hora_inicio DESC
    """, nativeQuery = true)
    List<Object[]> listarReservasParaAdminNative();

    // Método para filtrar reservas por texto y fecha
    @Query(value = """
    SELECT
        r.id,
        CONCAT(u.nombre, ' ', u.apellidos) AS usuarioNombre,
        i.nombre AS instalacionNombre,
        i.ubicacion AS instalacionUbicacion,
        r.metodo_pago AS metodoPago,
        i.imagen_url AS instalacionImagenUrl,
        r.fecha,
        r.hora_inicio AS horaInicio,
        r.hora_fin AS horaFin,
        r.estado,
        r.estado_pago AS estadoPago
    FROM reservas r
    JOIN usuarios u ON r.usuario_id = u.id
    JOIN instalaciones i ON r.instalacion_id = i.id
    WHERE (:texto IS NULL OR
        LOWER(CONCAT(u.nombre, ' ', u.apellidos)) LIKE LOWER(CONCAT('%', :texto, '%')) OR
        LOWER(i.nombre) LIKE LOWER(CONCAT('%', :texto, '%')))
    AND (:fecha IS NULL OR r.fecha = :fecha)
    ORDER BY r.fecha DESC, r.hora_inicio DESC
    """, nativeQuery = true)
    List<Object[]> filtrarReservasNative(@Param("texto") String texto, @Param("fecha") java.sql.Date fecha);

    // Método para filtrar reservas por texto y rango de fechas
    @Query(value = """
    SELECT
        r.id,
        CONCAT(u.nombre, ' ', u.apellidos) AS usuarioNombre,
        i.nombre AS instalacionNombre,
        i.ubicacion AS instalacionUbicacion,
        r.metodo_pago AS metodoPago,
        i.imagen_url AS instalacionImagenUrl,
        r.fecha,
        r.hora_inicio AS horaInicio,
        r.hora_fin AS horaFin,
        r.estado,
        r.estado_pago AS estadoPago
    FROM reservas r
    JOIN usuarios u ON r.usuario_id = u.id
    JOIN instalaciones i ON r.instalacion_id = i.id
    WHERE (:texto IS NULL OR
        LOWER(CONCAT(u.nombre, ' ', u.apellidos)) LIKE LOWER(CONCAT('%', :texto, '%')) OR
        LOWER(i.nombre) LIKE LOWER(CONCAT('%', :texto, '%')))
    AND (:fechaInicio IS NULL OR r.fecha >= :fechaInicio)
    AND (:fechaFin IS NULL OR r.fecha <= :fechaFin)
    ORDER BY r.fecha DESC, r.hora_inicio DESC
    """, nativeQuery = true)
    List<Object[]> filtrarReservasPorRangoNative(@Param("texto") String texto, @Param("fechaInicio") java.sql.Date fechaInicio, @Param("fechaFin") java.sql.Date fechaFin);    // Método para buscar reservas por el DNI del usuario
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
        CONCAT(
            SUBSTRING_INDEX(u.nombre, ' ', 1),
            ' ',
            SUBSTRING_INDEX(u.apellidos, ' ', 1)
        ) AS nombreUsuario,
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
    ORDER BY r.created_at DESC
    LIMIT 5
    """, nativeQuery = true)
    List<ReservaRecienteDTO> obtenerReservasRecientes();

    // Consulta para obtener los detalles de una reserva específica
    @Query("SELECT new com.example.deporsm.dto.ReservaDetalleDTO(" +
            "r.id, " +
            "u.id, " +
            "CONCAT(u.nombre, ' ', u.apellidos), " +
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
        CONCAT(u.nombre, ' ', u.apellidos) AS nombreUsuario,
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
     * Filtra por fecha de reserva (fecha) para mostrar reservas en el período seleccionado
     */
    @Query(value = """
    SELECT
        r.id,
        CONCAT(u.nombre, ' ', u.apellidos) as usuario,
        i.nombre as instalacion,
        DATE_FORMAT(r.fecha, '%d/%m/%Y') as fecha_reserva,
        TIME_FORMAT(r.hora_inicio, '%H:%i') as hora_inicio,
        TIME_FORMAT(r.hora_fin, '%H:%i') as hora_fin,
        r.estado,
        r.estado_pago,
        COALESCE(r.metodo_pago, '') as metodo_pago
    FROM reservas r
    JOIN usuarios u ON r.usuario_id = u.id
    JOIN instalaciones i ON r.instalacion_id = i.id
    WHERE r.fecha BETWEEN :fechaInicio AND :fechaFin
    AND (:instalacionId IS NULL OR r.instalacion_id = :instalacionId)
    ORDER BY r.id ASC
    """, nativeQuery = true)
    List<Object[]> findReservasForReport(
        @Param("fechaInicio") LocalDate fechaInicio,
        @Param("fechaFin") LocalDate fechaFin,
        @Param("instalacionId") Integer instalacionId);

    /**
     * Consulta para obtener datos de ingresos para reportes
     * Usa la fecha de los pagos (tabla pagos) para mostrar ingresos reales del período
     * Solo cuenta pagos efectivos (estado = 'pagado'), los reembolsados no se incluyen
     */
    @Query(value = """
    SELECT
        DATE_FORMAT(fecha_agrupada, '%d/%m/%Y') as fecha,
        instalacion,
        total_reservas,
        total_ingresos
    FROM (
        SELECT
            DATE(p.updated_at) as fecha_agrupada,
            i.nombre as instalacion,
            -- Contar solo reservas pagadas (reservas efectivas)
            SUM(CASE WHEN p.estado = 'pagado' THEN 1 ELSE 0 END) as total_reservas,
            -- Sumar solo los pagos efectivos (no reembolsados)
            SUM(CASE WHEN p.estado = 'pagado' THEN p.monto ELSE 0 END) as total_ingresos
        FROM pagos p
        JOIN reservas r ON p.reserva_id = r.id
        JOIN instalaciones i ON r.instalacion_id = i.id
        WHERE DATE(p.updated_at) BETWEEN :fechaInicio AND :fechaFin
        AND p.estado IN ('pagado', 'reembolsado')
        AND (:instalacionId IS NULL OR r.instalacion_id = :instalacionId)
        GROUP BY DATE(p.updated_at), i.id, i.nombre
        HAVING total_reservas > 0 OR total_ingresos > 0
    ) AS subquery
    ORDER BY fecha_agrupada ASC
    """, nativeQuery = true)
    List<Object[]> findIngresosForReport(
        @Param("fechaInicio") LocalDate fechaInicio,
        @Param("fechaFin") LocalDate fechaFin,
        @Param("instalacionId") Integer instalacionId);

    /**
     * Consulta para obtener datos de uso de instalaciones para reportes
     * Filtra por fecha de creación de la reserva para mostrar actividad de reservas del período
     * Solo cuenta reservas efectivamente pagadas
     */
    @Query(value = """
    SELECT
        i.nombre as instalacion,
        COUNT(p.id) as total_reservas,
        SUM(TIMESTAMPDIFF(MINUTE, r.hora_inicio, r.hora_fin)) / 60.0 as horas_reservadas,
        SUM(p.monto) as ingresos_generados,
        (SELECT horario FROM (
            SELECT
                CONCAT(TIME_FORMAT(r2.hora_inicio, '%H:%i'), ' - ', TIME_FORMAT(r2.hora_fin, '%H:%i')) as horario,
                COUNT(*) as total
            FROM reservas r2
            JOIN pagos p2 ON p2.reserva_id = r2.id
            WHERE r2.instalacion_id = i.id
            AND DATE(r2.created_at) BETWEEN :fechaInicio AND :fechaFin
            AND p2.estado = 'pagado'
            GROUP BY horario
            ORDER BY total DESC
            LIMIT 1
         ) as subquery) as horario_mas_popular
    FROM instalaciones i
    LEFT JOIN reservas r ON r.instalacion_id = i.id
        AND DATE(r.created_at) BETWEEN :fechaInicio AND :fechaFin
    LEFT JOIN pagos p ON p.reserva_id = r.id
        AND p.estado = 'pagado'
    WHERE (:instalacionId IS NULL OR i.id = :instalacionId)
    GROUP BY i.id, i.nombre
    HAVING total_reservas > 0
    ORDER BY total_reservas DESC
    """, nativeQuery = true)
    List<Object[]> findInstalacionesUsageForReport(
        @Param("fechaInicio") LocalDate fechaInicio,
        @Param("fechaFin") LocalDate fechaFin,
        @Param("instalacionId") Integer instalacionId);

    /**
     * Consulta para obtener datos de ingresos diarios para el dashboard
     * Muestra los últimos 30 días con datos diarios, incluyendo días sin ingresos
     */
    @Query(value = """
    WITH RECURSIVE DateRange AS (
        SELECT DATE_SUB(CURDATE(), INTERVAL 29 DAY) as date
        UNION ALL
        SELECT DATE_ADD(date, INTERVAL 1 DAY)
        FROM DateRange
        WHERE date < CURDATE()
    ),
    DailyIncome AS (
        SELECT
            r.fecha as fecha,
            SUM(
                CASE
                    WHEN r.estado_pago = 'pagado' THEN TIMESTAMPDIFF(MINUTE, r.hora_inicio, r.hora_fin) * i.precio / 60
                    WHEN r.estado_pago = 'reembolsado' THEN -TIMESTAMPDIFF(MINUTE, r.hora_inicio, r.hora_fin) * i.precio / 60
                    ELSE 0
                END
            ) as total_ingresos
        FROM reservas r
        JOIN instalaciones i ON r.instalacion_id = i.id
        WHERE r.estado_pago IN ('pagado', 'reembolsado')
        AND r.fecha >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
        GROUP BY r.fecha
    )
    SELECT
        DATE_FORMAT(d.date, '%d/%m') as fecha,
        COALESCE(di.total_ingresos, 0) as total_ingresos
    FROM DateRange d
    LEFT JOIN DailyIncome di ON d.date = di.fecha
    ORDER BY d.date ASC
    """, nativeQuery = true)
    List<Map<String, Object>> findIncomeByMonth();

    /**
     * Consulta para obtener datos de ingresos por instalación para el dashboard
     * Lógica: pagado suma, reembolsado resta, pendiente/fallido no cuentan
     */
    @Query(value = """
    SELECT
        i.nombre as nombre,
        COALESCE(SUM(
            CASE
                WHEN r.estado_pago = 'pagado' THEN TIMESTAMPDIFF(MINUTE, r.hora_inicio, r.hora_fin) * i.precio / 60
                WHEN r.estado_pago = 'reembolsado' THEN -TIMESTAMPDIFF(MINUTE, r.hora_inicio, r.hora_fin) * i.precio / 60
                ELSE 0
            END
        ), 0) as total_ingresos
    FROM instalaciones i
    LEFT JOIN reservas r ON i.id = r.instalacion_id
        AND r.fecha >= DATE_SUB(CURDATE(), INTERVAL 1 MONTH)
        AND r.estado_pago IN ('pagado', 'reembolsado')
    GROUP BY i.id, i.nombre
    ORDER BY total_ingresos DESC
    LIMIT 5
    """, nativeQuery = true)
    List<Map<String, Object>> findIncomeByFacility();

    /**
     * Consulta para obtener datos de reservas por día de la semana para el dashboard
     */
    @Query(value = """
    SELECT
        CASE DAYOFWEEK(r.fecha)
            WHEN 1 THEN 'Dom'
            WHEN 2 THEN 'Lun'
            WHEN 3 THEN 'Mar'
            WHEN 4 THEN 'Mié'
            WHEN 5 THEN 'Jue'
            WHEN 6 THEN 'Vie'
            WHEN 7 THEN 'Sáb'
        END as dia_semana,
        COUNT(*) as total_reservas
    FROM reservas r
    WHERE r.fecha >= DATE_SUB(CURDATE(), INTERVAL 3 MONTH)
    GROUP BY dia_semana, DAYOFWEEK(r.fecha)
    ORDER BY DAYOFWEEK(r.fecha)
    """, nativeQuery = true)
    List<Map<String, Object>> findReservationsByDayOfWeek();

    /**
     * Consulta para obtener datos de uso por hora para el dashboard
     * Modificada para ser completamente compatible con sql_mode=only_full_group_by
     */
    @Query(value = """
    SELECT
        horas.hora as hora,
        COALESCE(conteo.total_reservas, 0) as total_reservas
    FROM (
        SELECT 0 as hora_num, '00:00' as hora
        UNION SELECT 1, '01:00' UNION SELECT 2, '02:00' UNION SELECT 3, '03:00'
        UNION SELECT 4, '04:00' UNION SELECT 5, '05:00' UNION SELECT 6, '06:00'
        UNION SELECT 7, '07:00' UNION SELECT 8, '08:00' UNION SELECT 9, '09:00'
        UNION SELECT 10, '10:00' UNION SELECT 11, '11:00' UNION SELECT 12, '12:00'
        UNION SELECT 13, '13:00' UNION SELECT 14, '14:00' UNION SELECT 15, '15:00'
        UNION SELECT 16, '16:00' UNION SELECT 17, '17:00' UNION SELECT 18, '18:00'
        UNION SELECT 19, '19:00' UNION SELECT 20, '20:00' UNION SELECT 21, '21:00'
        UNION SELECT 22, '22:00' UNION SELECT 23, '23:00'
    ) horas
    LEFT JOIN (
        SELECT
            HOUR(hora_inicio) as hora_num,
            COUNT(*) as total_reservas
        FROM reservas
        WHERE fecha >= DATE_SUB(CURDATE(), INTERVAL 3 MONTH)
        GROUP BY HOUR(hora_inicio)
    ) conteo ON horas.hora_num = conteo.hora_num
    ORDER BY horas.hora_num
    """, nativeQuery = true)
    List<Map<String, Object>> findUsageByHour();

    /**
     * Consulta para obtener datos de reservas por estado para el dashboard
     * Asegurando que siempre se muestren los 4 estados (pendiente, confirmada, cancelada, completada)
     */
    @Query(value = """
    SELECT
        estados.estado as name,
        COALESCE(conteo.total, 0) as value
    FROM (
        SELECT 'pendiente' as estado
        UNION SELECT 'confirmada'
        UNION SELECT 'cancelada'
        UNION SELECT 'completada'
    ) estados
    LEFT JOIN (
        SELECT
            estado,
            COUNT(*) as total
        FROM reservas
        WHERE fecha >= DATE_SUB(CURDATE(), INTERVAL 3 MONTH)
        GROUP BY estado
    ) conteo ON estados.estado = conteo.estado
    ORDER BY
        CASE
            WHEN estados.estado = 'pendiente' THEN 1
            WHEN estados.estado = 'confirmada' THEN 2
            WHEN estados.estado = 'completada' THEN 3
            WHEN estados.estado = 'cancelada' THEN 4
            ELSE 5
        END
    """, nativeQuery = true)
    List<Map<String, Object>> findReservationsByStatus();

    /**
     * Busca reservas confirmadas cuya fecha y hora de fin ya hayan pasado
     * para marcarlas como completadas automáticamente
     */
    @Query("SELECT r FROM Reserva r WHERE r.estado = 'confirmada' " +
           "AND (r.fecha < :fechaActual OR " +
           "(r.fecha = :fechaActual AND r.horaFin <= :horaActual))")
    List<Reserva> findReservasConfirmadasVencidas(
        @Param("fechaActual") java.sql.Date fechaActual,
        @Param("horaActual") java.sql.Time horaActual
    );
}
