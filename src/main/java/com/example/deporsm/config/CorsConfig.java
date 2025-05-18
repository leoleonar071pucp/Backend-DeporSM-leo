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
                        .allowedOrigins(
                                "https://deporsm-apiwith-1035693188565.us-central1.run.app",
                                "https://frontend-depor-sm-pyrv6rxh1-leonardo-pucps-projects.vercel.app",
                                "https://frontend-depor-sm-leo.vercel.app",  // ✅ ESTE FALTABA EN EL FILTER
                                "http://localhost:3000"
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .exposedHeaders("Set-Cookie", "Authorization", "Content-Type")
                        .allowCredentials(true)
                        .maxAge(86400);
            }
        };
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);
        config.addAllowedOrigin("https://deporsm-apiwith-1035693188565.us-central1.run.app");
        config.addAllowedOrigin("https://frontend-depor-sm-pyrv6rxh1-leonardo-pucps-projects.vercel.app");
        config.addAllowedOrigin("https://frontend-depor-sm-leo.vercel.app"); // ✅ FALTABA ESTA LÍNEA
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
        config.setMaxAge(86400L);

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
