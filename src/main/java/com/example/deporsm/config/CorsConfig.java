package com.example.deporsm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    // Orígenes permitidos desde la configuración
    @Value("${app.cors.allowed-origins:http://localhost:3000}")
    private String allowedOriginsString;

    // Lista de encabezados expuestos
    private static final List<String> EXPOSED_HEADERS = Arrays.asList(
        "Set-Cookie",
        "Authorization",
        "Content-Type",
        "Access-Control-Allow-Origin",
        "Access-Control-Allow-Credentials"
    );

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // Convertir la cadena de orígenes separados por comas en un array
                String[] origins = allowedOriginsString.split(",");

                registry.addMapping("/**")
                        .allowedOriginPatterns(origins) // Usar allowedOriginPatterns en lugar de allowedOrigins
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .exposedHeaders(String.join(",", EXPOSED_HEADERS))
                        .allowCredentials(true)
                        .maxAge(86400);
            }
        };
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Habilitar credenciales
        config.setAllowCredentials(true);

        // Agregar todos los orígenes permitidos desde la configuración usando allowedOriginPatterns
        String[] origins = allowedOriginsString.split(",");
        for (String origin : origins) {
            config.addAllowedOriginPattern(origin.trim()); // Usar addAllowedOriginPattern en lugar de addAllowedOrigin
        }

        // Configurar encabezados y métodos
        config.addAllowedHeader("*");

        // Exponer encabezados específicos
        EXPOSED_HEADERS.forEach(config::addExposedHeader);

        // Permitir todos los métodos HTTP comunes
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("OPTIONS");

        // Tiempo de caché para respuestas preflight
        config.setMaxAge(86400L);

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
