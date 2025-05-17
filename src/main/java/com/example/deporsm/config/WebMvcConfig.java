package com.example.deporsm.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        try {
            Path comprobantesUploadDir = Paths.get("comprobantes");
            
            // Asegurarse de que el directorio existe
            if (!Files.exists(comprobantesUploadDir)) {
                Files.createDirectories(comprobantesUploadDir);
                System.out.println("Directorio de comprobantes creado desde WebMvcConfig: " + comprobantesUploadDir.toAbsolutePath().toString());
            }
            
            String comprobantesUploadPath = comprobantesUploadDir.toFile().getAbsolutePath();
            System.out.println("Configurando handler para recursos estáticos en: " + comprobantesUploadPath);
            
            registry.addResourceHandler("/comprobantes/**")
                    .addResourceLocations("file:" + comprobantesUploadPath + "/")
                    .setCachePeriod(0);  // Deshabilitar caché para depuración
            
            System.out.println("Resource handler configurado correctamente para /comprobantes/**");
        } catch (Exception e) {
            System.err.println("Error al configurar resource handler: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
