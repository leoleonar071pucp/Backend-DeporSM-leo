package com.example.deporsm.service;

import com.example.deporsm.dto.ContactoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Servicio para manejar los mensajes de contacto
 */
@Service
public class ContactoService {

    @Autowired
    private EmailService emailService;

    @Value("${contacto.email.destinatario:deportes@munisanmiguel.gob.pe}")
    private String emailDestinatario;

    /**
     * Procesa un mensaje de contacto y envía un correo electrónico
     *
     * @param contactoDTO Datos del mensaje de contacto
     * @throws MessagingException Si ocurre un error al enviar el correo
     */
    public void procesarMensajeContacto(ContactoDTO contactoDTO) throws MessagingException {
        System.out.println("[INFO] Iniciando procesamiento de mensaje de contacto para: " + contactoDTO.getEmail());
        System.out.println("[INFO] Destinatario configurado: " + emailDestinatario);

        try {
            // Enviar correo al administrador
            System.out.println("[INFO] Enviando correo al administrador...");
            enviarCorreoAdministrador(contactoDTO);
            System.out.println("[INFO] Correo al administrador enviado exitosamente");

            // Enviar correo de confirmación al usuario
            System.out.println("[INFO] Enviando correo de confirmación al usuario...");
            enviarCorreoConfirmacion(contactoDTO);
            System.out.println("[INFO] Correo de confirmación enviado exitosamente");
        } catch (MessagingException e) {
            System.out.println("[ERROR] Error al enviar correos: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-lanzar la excepción para que sea manejada por el controlador
        }
    }

    /**
     * Envía un correo al administrador con los datos del mensaje de contacto
     *
     * @param contactoDTO Datos del mensaje de contacto
     * @throws MessagingException Si ocurre un error al enviar el correo
     */
    private void enviarCorreoAdministrador(ContactoDTO contactoDTO) throws MessagingException {
        String asunto = "Nuevo mensaje de contacto: " + contactoDTO.getAsunto();

        String contenido = generarContenidoCorreoAdministrador(contactoDTO);

        emailService.sendHtmlEmail(emailDestinatario, asunto, contenido);
    }

    /**
     * Envía un correo de confirmación al usuario
     *
     * @param contactoDTO Datos del mensaje de contacto
     * @throws MessagingException Si ocurre un error al enviar el correo
     */
    private void enviarCorreoConfirmacion(ContactoDTO contactoDTO) throws MessagingException {
        String asunto = "Confirmación de mensaje - DeporSM";

        String contenido = generarContenidoCorreoConfirmacion(contactoDTO);

        emailService.sendHtmlEmail(contactoDTO.getEmail(), asunto, contenido);
    }

    /**
     * Genera el contenido HTML del correo para el administrador
     *
     * @param contactoDTO Datos del mensaje de contacto
     * @return Contenido HTML del correo
     */
    private String generarContenidoCorreoAdministrador(ContactoDTO contactoDTO) {
        LocalDateTime ahora = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        return "<html><body>" +
                "<h2>Nuevo mensaje de contacto</h2>" +
                "<p><strong>Fecha y hora:</strong> " + ahora.format(formatter) + "</p>" +
                "<p><strong>Nombre:</strong> " + contactoDTO.getNombre() + "</p>" +
                "<p><strong>Email:</strong> " + contactoDTO.getEmail() + "</p>" +
                "<p><strong>Teléfono:</strong> " + (contactoDTO.getTelefono() != null ? contactoDTO.getTelefono() : "No proporcionado") + "</p>" +
                "<p><strong>Asunto:</strong> " + contactoDTO.getAsunto() + "</p>" +
                "<p><strong>Mensaje:</strong></p>" +
                "<p style='padding: 10px; background-color: #f5f5f5; border-left: 4px solid #0066cc;'>" +
                contactoDTO.getMensaje().replace("\n", "<br>") +
                "</p>" +
                "</body></html>";
    }

    /**
     * Genera el contenido HTML del correo de confirmación para el usuario
     *
     * @param contactoDTO Datos del mensaje de contacto
     * @return Contenido HTML del correo
     */
    private String generarContenidoCorreoConfirmacion(ContactoDTO contactoDTO) {
        return "<html><body>" +
                "<h2>Hemos recibido tu mensaje</h2>" +
                "<p>Hola " + contactoDTO.getNombre() + ",</p>" +
                "<p>Gracias por contactarnos. Hemos recibido tu mensaje y te responderemos a la brevedad posible.</p>" +
                "<p><strong>Asunto:</strong> " + contactoDTO.getAsunto() + "</p>" +
                "<p><strong>Mensaje:</strong></p>" +
                "<p style='padding: 10px; background-color: #f5f5f5; border-left: 4px solid #0066cc;'>" +
                contactoDTO.getMensaje().replace("\n", "<br>") +
                "</p>" +
                "<p>Saludos cordiales,</p>" +
                "<p><strong>Equipo DeporSM</strong><br>" +
                "Municipalidad de San Miguel</p>" +
                "</body></html>";
    }
}
