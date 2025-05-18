package com.example.deporsm.repository;

import com.example.deporsm.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Integer> {
    
    /**
     * Encuentra todas las notificaciones de un usuario ordenadas por fecha de envío descendente
     * @param usuarioId ID del usuario
     * @return Lista de notificaciones
     */
    @Query("SELECT n FROM Notificacion n WHERE n.usuario.id = :usuarioId ORDER BY n.fechaEnvio DESC")
    List<Notificacion> findByUsuarioIdOrderByFechaEnvioDesc(@Param("usuarioId") Integer usuarioId);
    
    /**
     * Encuentra todas las notificaciones no leídas de un usuario
     * @param usuarioId ID del usuario
     * @return Lista de notificaciones no leídas
     */
    @Query("SELECT n FROM Notificacion n WHERE n.usuario.id = :usuarioId AND n.leida = false ORDER BY n.fechaEnvio DESC")
    List<Notificacion> findByUsuarioIdAndLeidaFalseOrderByFechaEnvioDesc(@Param("usuarioId") Integer usuarioId);
    
    /**
     * Encuentra todas las notificaciones leídas de un usuario
     * @param usuarioId ID del usuario
     * @return Lista de notificaciones leídas
     */
    @Query("SELECT n FROM Notificacion n WHERE n.usuario.id = :usuarioId AND n.leida = true ORDER BY n.fechaEnvio DESC")
    List<Notificacion> findByUsuarioIdAndLeidaTrueOrderByFechaEnvioDesc(@Param("usuarioId") Integer usuarioId);
    
    /**
     * Marca todas las notificaciones de un usuario como leídas
     * @param usuarioId ID del usuario
     */
    @Modifying
    @Transactional
    @Query("UPDATE Notificacion n SET n.leida = true WHERE n.usuario.id = :usuarioId AND n.leida = false")
    void markAllAsReadByUsuarioId(@Param("usuarioId") Integer usuarioId);
    
    /**
     * Elimina todas las notificaciones leídas de un usuario
     * @param usuarioId ID del usuario
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Notificacion n WHERE n.usuario.id = :usuarioId AND n.leida = true")
    void deleteAllReadByUsuarioId(@Param("usuarioId") Integer usuarioId);
}
