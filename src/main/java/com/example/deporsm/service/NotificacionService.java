package com.example.deporsm.service;

import com.example.deporsm.dto.NotificacionDTO;
import com.example.deporsm.model.Notificacion;
import com.example.deporsm.model.PreferenciaNotificacion;
import com.example.deporsm.model.Usuario;
import com.example.deporsm.repository.NotificacionRepository;
import com.example.deporsm.repository.PreferenciaNotificacionRepository;
import com.example.deporsm.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.MessagingException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NotificacionService {

    @Autowired
    private NotificacionRepository notificacionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PreferenciaNotificacionRepository preferenciaNotificacionRepository;

    @Autowired
    private EmailService emailService;

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
            Usuario usuario = usuarioOpt.get();

            // Crear y guardar la notificación en la base de datos
            Notificacion notificacion = new Notificacion();
            notificacion.setUsuario(usuario);
            notificacion.setTitulo(titulo);
            notificacion.setMensaje(mensaje);
            notificacion.setTipo(tipo);
            notificacion.setLeida(false);
            notificacion.setFechaEnvio(new Timestamp(System.currentTimeMillis()));
            notificacion.setCategoria(categoria);
            notificacion.setFeedback(feedback);

            Notificacion guardada = notificacionRepository.save(notificacion);

            // Verificar si el usuario tiene habilitadas las notificaciones por email
            enviarNotificacionPorEmail(usuario, titulo, mensaje);

            return convertirADTO(guardada);
        }

        return null;
    }

    /**
     * Método simplificado para crear una notificación con solo los campos esenciales
     * @param usuarioId ID del usuario
     * @param titulo Título de la notificación
     * @param mensaje Mensaje de la notificación
     * @param tipo Tipo de notificación
     * @return La notificación creada
     */
    @Transactional
    public NotificacionDTO crearNotificacion(Integer usuarioId, String titulo, String mensaje, String tipo) {
        return crearNotificacion(usuarioId, titulo, mensaje, tipo, null, null);
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

    /**
     * Envía una notificación por correo electrónico si el usuario tiene habilitadas las notificaciones por email
     * @param usuario Usuario destinatario
     * @param titulo Título de la notificación
     * @param mensaje Mensaje de la notificación
     */
    private void enviarNotificacionPorEmail(Usuario usuario, String titulo, String mensaje) {
        try {
            // Verificar si el usuario tiene habilitadas las notificaciones por email
            Optional<PreferenciaNotificacion> preferenciaOpt = preferenciaNotificacionRepository.findByUsuarioId(usuario.getId());

            boolean enviarEmail = false;
            if (preferenciaOpt.isPresent()) {
                PreferenciaNotificacion preferencia = preferenciaOpt.get();
                enviarEmail = preferencia.getEmail() != null && preferencia.getEmail();
            } else {
                // Si no hay preferencias configuradas, asumir que sí quiere recibir emails (valor por defecto)
                enviarEmail = true;
            }

            if (enviarEmail && usuario.getEmail() != null && !usuario.getEmail().isEmpty()) {
                String asunto = "Nueva notificación - DeporSM";
                String contenidoHtml = generarContenidoEmailNotificacion(usuario, titulo, mensaje);

                emailService.sendHtmlEmail(usuario.getEmail(), asunto, contenidoHtml);
                System.out.println("[INFO] Notificación enviada por email a: " + usuario.getEmail());
            }
        } catch (MessagingException e) {
            System.err.println("[ERROR] Error al enviar notificación por email a " + usuario.getEmail() + ": " + e.getMessage());
            // No lanzar excepción para no afectar la creación de la notificación en la app
        } catch (Exception e) {
            System.err.println("[ERROR] Error inesperado al enviar notificación por email: " + e.getMessage());
        }
    }

    /**
     * Genera el contenido HTML para el correo de notificación
     * @param usuario Usuario destinatario
     * @param titulo Título de la notificación
     * @param mensaje Mensaje de la notificación
     * @return Contenido HTML del correo
     */
    private String generarContenidoEmailNotificacion(Usuario usuario, String titulo, String mensaje) {
        return """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Nueva Notificación - DeporSM</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; background-color: #f4f4f4; }
                    .container { max-width: 600px; margin: 0 auto; background-color: #ffffff; padding: 20px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }
                    .header { background-color: #2563eb; color: white; padding: 20px; text-align: center; border-radius: 10px 10px 0 0; margin: -20px -20px 20px -20px; }
                    .content { padding: 20px 0; }
                    .notification-box { background-color: #f8fafc; border-left: 4px solid #2563eb; padding: 15px; margin: 20px 0; border-radius: 5px; }
                    .footer { text-align: center; margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee; color: #666; font-size: 12px; }
                    .button { display: inline-block; background-color: #2563eb; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>DeporSM</h1>
                        <p>Sistema de Gestión Deportiva - Municipalidad de San Miguel</p>
                    </div>

                    <div class="content">
                        <h2>¡Hola %s!</h2>
                        <p>Tienes una nueva notificación en tu cuenta de DeporSM:</p>

                        <div class="notification-box">
                            <h3>%s</h3>
                            <p>%s</p>
                        </div>

                        <p>Puedes revisar todas tus notificaciones iniciando sesión en la plataforma.</p>
                    </div>

                    <div class="footer">
                        <p>Este es un correo automático, por favor no responder.</p>
                        <p>© 2024 Municipalidad de San Miguel - DeporSM</p>
                        <p>Si no deseas recibir estas notificaciones, puedes desactivarlas desde tu perfil en la aplicación.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                usuario.getNombre() != null ? usuario.getNombre() : "Usuario",
                titulo,
                mensaje
            );
    }
}
