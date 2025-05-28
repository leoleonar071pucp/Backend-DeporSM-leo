package com.example.deporsm.service;

import com.example.deporsm.model.PasswordResetToken;
import com.example.deporsm.model.Usuario;
import com.example.deporsm.repository.PasswordResetTokenRepository;
import com.example.deporsm.repository.UsuarioRepository;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class PasswordResetService {

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    /**
     * Crea un token de restablecimiento de contraseña y envía un correo al usuario
     *
     * @param email Email del usuario
     * @return true si se envió el correo, false si no se encontró el usuario
     * @throws MessagingException Si ocurre un error al enviar el correo
     */
    @Transactional
    public boolean solicitarRestablecimientoPassword(String email) throws MessagingException {
        System.out.println("[INFO] Solicitando restablecimiento de contraseña para: " + email);

        // Buscar usuario por email
        return usuarioRepository.findByEmail(email)
                .map(usuario -> {
                    try {
                        // Invalidar tokens anteriores
                        tokenRepository.invalidateAllTokensByUsuarioId(usuario.getId());

                        // Generar nuevo token
                        String token = UUID.randomUUID().toString();
                        PasswordResetToken resetToken = new PasswordResetToken(usuario, token);
                        tokenRepository.save(resetToken);

                        // Enviar correo con el token
                        enviarCorreoRestablecimiento(usuario, token);

                        System.out.println("[INFO] Token de restablecimiento creado y correo enviado para: " + email);
                        return true;
                    } catch (MessagingException e) {
                        System.out.println("[ERROR] Error al enviar correo de restablecimiento: " + e.getMessage());
                        throw new RuntimeException("Error al enviar correo de restablecimiento", e);
                    }
                })
                .orElseGet(() -> {
                    System.out.println("[INFO] No se encontró usuario con email: " + email);
                    return false;
                });
    }

    /**
     * Valida un token de restablecimiento de contraseña
     *
     * @param token Token a validar
     * @return true si el token es válido, false si no
     */
    public boolean validarToken(String token) {
        System.out.println("[INFO] Validando token de restablecimiento: " + token);

        return tokenRepository.findByToken(token)
                .map(resetToken -> {
                    boolean valido = resetToken.isValido();
                    System.out.println("[INFO] Token " + (valido ? "válido" : "inválido"));
                    return valido;
                })
                .orElseGet(() -> {
                    System.out.println("[INFO] Token no encontrado");
                    return false;
                });
    }

    /**
     * Restablece la contraseña de un usuario usando un token
     *
     * @param token Token de restablecimiento
     * @param nuevaPassword Nueva contraseña
     * @return true si se restableció la contraseña, false si el token es inválido
     */
    @Transactional
    public boolean restablecerPassword(String token, String nuevaPassword) {
        System.out.println("[INFO] Restableciendo contraseña con token: " + token);

        return tokenRepository.findByToken(token)
                .filter(PasswordResetToken::isValido)
                .map(resetToken -> {
                    Usuario usuario = resetToken.getUsuario();

                    // Actualizar contraseña
                    usuario.setPassword(passwordEncoder.encode(nuevaPassword));
                    usuarioRepository.save(usuario);

                    // Marcar token como usado
                    resetToken.setUsado(true);
                    tokenRepository.save(resetToken);

                    System.out.println("[INFO] Contraseña restablecida para usuario: " + usuario.getEmail());
                    return true;
                })
                .orElseGet(() -> {
                    System.out.println("[INFO] Token inválido o no encontrado");
                    return false;
                });
    }

    /**
     * Envía un correo con el enlace para restablecer la contraseña
     *
     * @param usuario Usuario destinatario
     * @param token Token de restablecimiento
     * @throws MessagingException Si ocurre un error al enviar el correo
     */
    private void enviarCorreoRestablecimiento(Usuario usuario, String token) throws MessagingException {
        String resetUrl = frontendUrl + "/resetear-contrasena?token=" + token;
        String asunto = "Restablecimiento de contraseña - DeporSM";
        String contenido = generarContenidoCorreoRestablecimiento(usuario, resetUrl);

        emailService.sendHtmlEmail(usuario.getEmail(), asunto, contenido);
    }

    /**
     * Genera el contenido HTML del correo de restablecimiento
     *
     * @param usuario Usuario destinatario
     * @param resetUrl URL para restablecer la contraseña
     * @return Contenido HTML del correo
     */
    private String generarContenidoCorreoRestablecimiento(Usuario usuario, String resetUrl) {
        // Usar la zona horaria de Perú (GMT-5)
        LocalDateTime expiracion = LocalDateTime.now(java.time.ZoneId.of("America/Lima")).plusHours(24);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        // Extraer primer nombre y primer apellido
        String primerNombre = extraerPrimerNombre(usuario.getNombre());
        String primerApellido = extraerPrimerApellido(usuario.getApellidos());
        String nombreCompleto = primerNombre + " " + primerApellido;

        return "<html><body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>" +
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e1e1e1; border-radius: 5px;'>" +
                "<div style='text-align: center; margin-bottom: 20px;'>" +
                "<h2 style='color: #0066cc;'>Restablecimiento de Contraseña</h2>" +
                "</div>" +
                "<p>Hola <strong>" + nombreCompleto + "</strong>,</p>" +
                "<p>Hemos recibido una solicitud para restablecer la contraseña de tu cuenta en DeporSM.</p>" +
                "<p>Para continuar con el proceso, haz clic en el siguiente botón:</p>" +
                "<div style='text-align: center; margin: 30px 0;'>" +
                "<a href='" + resetUrl + "' style='background-color: #0066cc; color: white; padding: 12px 20px; text-decoration: none; border-radius: 4px; font-weight: bold;'>Restablecer Contraseña</a>" +
                "</div>" +
                "<p>O copia y pega el siguiente enlace en tu navegador:</p>" +
                "<p style='word-break: break-all; background-color: #f5f5f5; padding: 10px; border-radius: 4px;'>" + resetUrl + "</p>" +
                "<p><strong>Nota:</strong> Este enlace expirará el " + expiracion.format(formatter) + ".</p>" +
                "<p>Si no solicitaste este cambio, puedes ignorar este correo y tu contraseña seguirá siendo la misma.</p>" +
                "<hr style='margin: 30px 0; border: none; border-top: 1px solid #e1e1e1;'>" +
                "<p style='font-size: 12px; color: #777; text-align: center;'>Este es un correo automático, por favor no respondas a este mensaje.<br>" +
                "DeporSM - Municipalidad de San Miguel</p>" +
                "</div>" +
                "</body></html>";
    }

    /**
     * Tarea programada para eliminar tokens expirados cada día
     */
    @Scheduled(cron = "0 0 0 * * ?") // Ejecutar a medianoche todos los días
    @Transactional
    public void limpiarTokensExpirados() {
        try {
            Timestamp ahora = new Timestamp(System.currentTimeMillis());
            tokenRepository.deleteExpiredTokens(ahora);
            System.out.println("[INFO] Limpieza de tokens expirados completada");
        } catch (Exception e) {
            System.err.println("[ERROR] Error al limpiar tokens expirados: " + e.getMessage());
        }
    }

    /**
     * Extrae el primer nombre de una cadena de nombres
     * @param nombres Cadena con uno o más nombres separados por espacios
     * @return El primer nombre
     */
    private String extraerPrimerNombre(String nombres) {
        if (nombres == null || nombres.trim().isEmpty()) {
            return "";
        }
        String[] partesNombre = nombres.trim().split("\\s+");
        return partesNombre[0];
    }

    /**
     * Extrae el primer apellido de una cadena de apellidos
     * @param apellidos Cadena con uno o más apellidos separados por espacios
     * @return El primer apellido
     */
    private String extraerPrimerApellido(String apellidos) {
        if (apellidos == null || apellidos.trim().isEmpty()) {
            return "";
        }
        String[] partesApellido = apellidos.trim().split("\\s+");
        return partesApellido[0];
    }
}
