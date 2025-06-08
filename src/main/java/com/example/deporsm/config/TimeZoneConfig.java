package com.example.deporsm.config;

import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;
import java.util.TimeZone;

/**
 * Configuración de zona horaria para la aplicación
 * Establece la zona horaria por defecto a America/Lima (GMT-5)
 */
@Configuration
public class TimeZoneConfig {

    @PostConstruct
    public void init() {
        // Establecer la zona horaria por defecto de la JVM a Peru/Lima
        TimeZone.setDefault(TimeZone.getTimeZone("America/Lima"));
        System.out.println("Zona horaria configurada: " + TimeZone.getDefault().getID());
    }
}
