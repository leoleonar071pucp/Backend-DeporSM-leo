package com.example.deporsm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    // Orígenes permitidos desde la configuración
    @Value("${app.cors.allowed-origins:}")
    private String allowedOriginsString;

    // Lista de encabezados expuestos
    private static final List<String> EXPOSED_HEADERS = Arrays.asList(
        "Set-Cookie",
        "Authorization",
        "Content-Type",
        "Access-Control-Allow-Origin",
        "Access-Control-Allow-Credentials"
    );

    /**
     * Configuración CORS principal usando CorsFilter para máximo control
     */
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // Configurar credenciales
        config.setAllowCredentials(true);
        
        // Determinar orígenes permitidos de forma segura
        String safeOrigins = getSafeOrigins();
        System.out.println("🌐 CORS: Configurando orígenes permitidos: " + safeOrigins);
        
        // Dividir y agregar orígenes
        String[] origins = safeOrigins.split(",");
        for (String origin : origins) {
            String trimmedOrigin = origin.trim();
            if (!trimmedOrigin.isEmpty() && !trimmedOrigin.equals("*")) {
                config.addAllowedOriginPattern(trimmedOrigin);
                System.out.println("✅ CORS: Agregado origen: " + trimmedOrigin);
            }
        }
        
        // Configurar headers
        config.addAllowedHeader("*");
        EXPOSED_HEADERS.forEach(config::addExposedHeader);
        
        // Configurar métodos
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("OPTIONS");
        config.addAllowedMethod("PATCH");
        
        // Cache preflight
        config.setMaxAge(86400L);
        
        // Registrar configuración
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
    
    /**
     * Obtiene orígenes seguros, nunca devuelve "*"
     */
    private String getSafeOrigins() {
        // Si la configuración está vacía, null o es "*", usar localhost
        if (allowedOriginsString == null || 
            allowedOriginsString.trim().isEmpty() || 
            allowedOriginsString.trim().equals("*")) {
            return "http://localhost:3000";
        }
        
        // Si contiene "*", reemplazar con localhost (medida de seguridad extra)
        String sanitized = allowedOriginsString.replaceAll("\\*", "http://localhost:3000");
        
        // Asegurar que no quede vacío
        return sanitized.trim().isEmpty() ? "http://localhost:3000" : sanitized;
    }
}
