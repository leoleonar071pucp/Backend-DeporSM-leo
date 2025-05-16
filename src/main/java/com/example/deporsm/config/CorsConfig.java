package com.example.deporsm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:3000")
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

        config.setAllowCredentials(true);
        config.addAllowedOrigin("http://localhost:3000");
        config.addAllowedHeader("*");
        config.addExposedHeader("Set-Cookie");
        config.addExposedHeader("Authorization");
        config.addExposedHeader("Content-Type");
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("OPTIONS");
        config.setMaxAge(86400L); // 24 horas

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}