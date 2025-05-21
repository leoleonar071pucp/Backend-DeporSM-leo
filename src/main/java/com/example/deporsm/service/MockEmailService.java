package com.example.deporsm.service;

import jakarta.mail.MessagingException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Servicio de correo simulado para desarrollo
 * Este servicio no envía correos reales, solo los registra en la consola
 */
@Service
@Profile("dev")
public class MockEmailService extends EmailService {

    public MockEmailService() {
        super(null); // No necesitamos JavaMailSender real
    }

    @Override
    public void sendSimpleEmail(String to, String subject, String text) {
        System.out.println("\n==================================================");
        System.out.println("CORREO SIMULADO (TEXTO PLANO)");
        System.out.println("==================================================");
        System.out.println("Para: " + to);
        System.out.println("Asunto: " + subject);
        System.out.println("--------------------------------------------------");
        System.out.println(text);
        System.out.println("==================================================\n");
    }

    @Override
    public void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        System.out.println("\n==================================================");
        System.out.println("CORREO SIMULADO (HTML)");
        System.out.println("==================================================");
        System.out.println("Para: " + to);
        System.out.println("Asunto: " + subject);
        System.out.println("--------------------------------------------------");
        System.out.println(htmlContent);
        System.out.println("==================================================\n");
        
        // Simular éxito
        System.out.println("✅ Correo simulado enviado exitosamente a: " + to);
    }
}
