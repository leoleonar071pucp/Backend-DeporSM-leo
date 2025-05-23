package com.example.deporsm.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * Servicio para enviar correos electrónicos
 */
@Service
@Profile("!dev") // Esta implementación se usa en todos los perfiles excepto dev
public class EmailService {

    protected final JavaMailSender mailSender;

    @Value("${spring.mail.username:deporsm.notificaciones@gmail.com}")
    protected String fromEmail;

    @Value("${spring.mail.sender-name:DeporSM - Municipalidad de San Miguel}")
    protected String senderName;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Envía un correo electrónico simple
     *
     * @param to Destinatario
     * @param subject Asunto
     * @param text Contenido del correo
     */
    public void sendSimpleEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(String.format("%s <%s>", senderName, fromEmail));
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    /**
     * Envía un correo electrónico con formato HTML
     *
     * @param to Destinatario
     * @param subject Asunto
     * @param htmlContent Contenido HTML del correo
     * @throws MessagingException Si ocurre un error al enviar el correo
     */
    public void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        System.out.println("[INFO] Preparando correo HTML para enviar a: " + to);
        System.out.println("[INFO] Usando cuenta de correo: " + fromEmail);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Usar solo el correo como remitente para evitar problemas de formato
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            System.out.println("[INFO] Enviando correo...");
            mailSender.send(message);
            System.out.println("[INFO] Correo enviado exitosamente a: " + to);
        } catch (MailAuthenticationException e) {
            System.out.println("[ERROR] Error de autenticación al enviar correo: " + e.getMessage());
            System.out.println("[ERROR] Verifica las credenciales de correo en application.properties");
            e.printStackTrace();
            throw new MessagingException("Error de autenticación al enviar correo. Verifica las credenciales.", e);
        } catch (MailSendException e) {
            System.out.println("[ERROR] Error al enviar correo: " + e.getMessage());
            System.out.println("[ERROR] Causa: " + (e.getCause() != null ? e.getCause().getMessage() : "Desconocida"));
            e.printStackTrace();
            throw new MessagingException("Error al enviar correo: " + e.getMessage(), e);
        } catch (MessagingException e) {
            System.out.println("[ERROR] Error al enviar correo HTML: " + e.getMessage());
            System.out.println("[ERROR] Causa: " + (e.getCause() != null ? e.getCause().getMessage() : "Desconocida"));
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            System.out.println("[ERROR] Error inesperado al enviar correo: " + e.getMessage());
            e.printStackTrace();
            throw new MessagingException("Error inesperado al enviar correo: " + e.getMessage(), e);
        }
    }
}
