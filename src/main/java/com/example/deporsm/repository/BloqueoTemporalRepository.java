package com.example.deporsm.repository;

import com.example.deporsm.model.BloqueoTemporal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface BloqueoTemporalRepository extends JpaRepository<BloqueoTemporal, Integer> {

    /**
     * Busca bloqueos temporales activos para una instalación, fecha y horario específicos
     */
    @Query("SELECT b FROM BloqueoTemporal b " +
           "WHERE b.instalacion.id = :instalacionId " +
           "AND b.fecha = :fecha " +
           "AND b.expiracion > :ahora " +
           "AND ((b.horaInicio <= :horaInicio AND b.horaFin > :horaInicio) " +
           "OR (b.horaInicio < :horaFin AND b.horaFin >= :horaFin) " +
           "OR (b.horaInicio >= :horaInicio AND b.horaFin <= :horaFin))")
    List<BloqueoTemporal> findActiveBlocksForTimeSlot(
            @Param("instalacionId") Integer instalacionId,
            @Param("fecha") Date fecha,
            @Param("horaInicio") Time horaInicio,
            @Param("horaFin") Time horaFin,
            @Param("ahora") Timestamp ahora);

    /**
     * Busca un bloqueo temporal por su token
     */
    Optional<BloqueoTemporal> findByToken(String token);

    /**
     * Elimina los bloqueos temporales expirados
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM BloqueoTemporal b WHERE b.expiracion <= :ahora")
    void deleteExpiredBlocks(@Param("ahora") Timestamp ahora);

    /**
     * Elimina los bloqueos temporales de un usuario específico
     */
    @Modifying
    @Transactional
    void deleteByUsuarioId(Integer usuarioId);
}
