package com.example.deporsm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class CorsConfig {
      @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*")  // ⚠️ solo si el backend no maneja cookies ni seguridad
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // Métodos específicos
                        .allowedHeaders("*")
                        .exposedHeaders("Set-Cookie", "Authorization", "Content-Type")
                        .allowCredentials(true)
                        .maxAge(86400); // Caché de preflight por 24 horas
            }
        };
    }
      // Filtro CORS adicional para garantizar que los preflight OPTIONS se manejen correctamente
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of("*"));  // Permitir todos los orígenes
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowCredentials(false); // ✅ obligatorio si usas "*"
        config.setMaxAge(86400L);

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

}