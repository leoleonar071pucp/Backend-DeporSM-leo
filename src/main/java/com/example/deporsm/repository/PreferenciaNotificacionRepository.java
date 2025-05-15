package com.example.deporsm.repository;

import com.example.deporsm.model.PreferenciaNotificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PreferenciaNotificacionRepository extends JpaRepository<PreferenciaNotificacion, Integer> {
    
    /**
     * Encuentra las preferencias de notificación de un usuario por su ID
     * @param usuarioId ID del usuario
     * @return Las preferencias de notificación o empty si no existen
     */
    Optional<PreferenciaNotificacion> findByUsuarioId(Integer usuarioId);
}
