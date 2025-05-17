package com.example.deporsm.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("https://deporsm-apiwith-1035693188565.us-central1.run.app", 
                               "https://frontend-depor-sm-pyrv6rxh1-leonardo-pucps-projects.vercel.app", 
                               "http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Set-Cookie", "Authorization", "Content-Type")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
