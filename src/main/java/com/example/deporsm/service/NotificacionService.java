package com.example.deporsm.service;

import com.example.deporsm.dto.NotificacionDTO;
import com.example.deporsm.model.Notificacion;
import com.example.deporsm.model.Usuario;
import com.example.deporsm.repository.NotificacionRepository;
import com.example.deporsm.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NotificacionService {

    @Autowired
    private NotificacionRepository notificacionRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    /**
     * Obtiene todas las notificaciones de un usuario
     * @param usuarioId ID del usuario
     * @return Lista de notificaciones
     */
    public List<NotificacionDTO> obtenerNotificacionesPorUsuario(Integer usuarioId) {
        List<Notificacion> notificaciones = notificacionRepository.findByUsuarioIdOrderByFechaEnvioDesc(usuarioId);
        return convertirADTO(notificaciones);
    }
    
    /**
     * Obtiene todas las notificaciones no leídas de un usuario
     * @param usuarioId ID del usuario
     * @return Lista de notificaciones no leídas
     */
    public List<NotificacionDTO> obtenerNotificacionesNoLeidasPorUsuario(Integer usuarioId) {
        List<Notificacion> notificaciones = notificacionRepository.findByUsuarioIdAndLeidaFalseOrderByFechaEnvioDesc(usuarioId);
        return convertirADTO(notificaciones);
    }
    
    /**
     * Obtiene todas las notificaciones leídas de un usuario
     * @param usuarioId ID del usuario
     * @return Lista de notificaciones leídas
     */
    public List<NotificacionDTO> obtenerNotificacionesLeidasPorUsuario(Integer usuarioId) {
        List<Notificacion> notificaciones = notificacionRepository.findByUsuarioIdAndLeidaTrueOrderByFechaEnvioDesc(usuarioId);
        return convertirADTO(notificaciones);
    }
    
    /**
     * Marca una notificación como leída
     * @param notificacionId ID de la notificación
     * @param usuarioId ID del usuario
     * @return true si se marcó correctamente, false si no
     */
    @Transactional
    public boolean marcarComoLeida(Integer notificacionId, Integer usuarioId) {
        Optional<Notificacion> notificacionOpt = notificacionRepository.findById(notificacionId);
        
        if (notificacionOpt.isPresent()) {
            Notificacion notificacion = notificacionOpt.get();
            
            // Verificar que la notificación pertenezca al usuario
            if (notificacion.getUsuario().getId().equals(usuarioId)) {
                notificacion.setLeida(true);
                notificacionRepository.save(notificacion);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Marca todas las notificaciones de un usuario como leídas
     * @param usuarioId ID del usuario
     */
    @Transactional
    public void marcarTodasComoLeidas(Integer usuarioId) {
        notificacionRepository.markAllAsReadByUsuarioId(usuarioId);
    }
    
    /**
     * Elimina una notificación
     * @param notificacionId ID de la notificación
     * @param usuarioId ID del usuario
     * @return true si se eliminó correctamente, false si no
     */
    @Transactional
    public boolean eliminarNotificacion(Integer notificacionId, Integer usuarioId) {
        Optional<Notificacion> notificacionOpt = notificacionRepository.findById(notificacionId);
        
        if (notificacionOpt.isPresent()) {
            Notificacion notificacion = notificacionOpt.get();
            
            // Verificar que la notificación pertenezca al usuario
            if (notificacion.getUsuario().getId().equals(usuarioId)) {
                notificacionRepository.delete(notificacion);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Elimina todas las notificaciones leídas de un usuario
     * @param usuarioId ID del usuario
     */
    @Transactional
    public void eliminarTodasLasLeidas(Integer usuarioId) {
        notificacionRepository.deleteAllReadByUsuarioId(usuarioId);
    }
    
    /**
     * Crea una nueva notificación para un usuario
     * @param usuarioId ID del usuario
     * @param titulo Título de la notificación
     * @param mensaje Mensaje de la notificación
     * @param tipo Tipo de notificación
     * @param categoria Categoría de la notificación (opcional)
     * @param feedback Feedback adicional (opcional)
     * @return La notificación creada
     */
    @Transactional
    public NotificacionDTO crearNotificacion(Integer usuarioId, String titulo, String mensaje, String tipo, String categoria, String feedback) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);
        
        if (usuarioOpt.isPresent()) {
            Notificacion notificacion = new Notificacion();
            notificacion.setUsuario(usuarioOpt.get());
            notificacion.setTitulo(titulo);
            notificacion.setMensaje(mensaje);
            notificacion.setTipo(tipo);
            notificacion.setLeida(false);
            notificacion.setFechaEnvio(new Timestamp(System.currentTimeMillis()));
            notificacion.setCategoria(categoria);
            notificacion.setFeedback(feedback);
            
            Notificacion guardada = notificacionRepository.save(notificacion);
            return convertirADTO(guardada);
        }
        
        return null;
    }
    
    /**
     * Convierte una lista de entidades Notificacion a DTOs
     * @param notificaciones Lista de notificaciones
     * @return Lista de DTOs
     */
    private List<NotificacionDTO> convertirADTO(List<Notificacion> notificaciones) {
        return notificaciones.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Convierte una entidad Notificacion a DTO
     * @param notificacion Notificación
     * @return DTO
     */
    private NotificacionDTO convertirADTO(Notificacion notificacion) {
        NotificacionDTO dto = new NotificacionDTO();
        dto.setId(notificacion.getId());
        dto.setTitulo(notificacion.getTitulo());
        dto.setMensaje(notificacion.getMensaje());
        dto.setTipo(notificacion.getTipo());
        dto.setLeida(notificacion.getLeida());
        
        // Formatear la fecha
        if (notificacion.getFechaEnvio() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            dto.setFechaEnvio(sdf.format(notificacion.getFechaEnvio()));
        }
        
        dto.setCategoria(notificacion.getCategoria());
        dto.setFeedback(notificacion.getFeedback());
        
        return dto;
    }
}
