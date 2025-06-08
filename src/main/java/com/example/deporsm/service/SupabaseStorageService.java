package com.example.deporsm.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

/**
 * Servicio para manejar la subida de archivos a Supabase Storage
 */
@Service
public class SupabaseStorageService {

    @Value("${supabase.url:https://goajrdpkfhunnfuqtoub.supabase.co}")
    private String supabaseUrl;

    @Value("${supabase.key:eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImdvYWpyZHBrZmh1bm5mdXF0b3ViIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY3MTU1NTQsImV4cCI6MjA2MjI5MTU1NH0.-_GxSWv-1UZNsXcSwIcFUKlprJ5LMX_0iz5VbesGgPQ}")
    private String supabaseKey;

    private final WebClient webClient;

    public SupabaseStorageService() {
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(50 * 1024 * 1024)) // 50MB
                .build();
    }

    /**
     * Sube un archivo de reporte a Supabase Storage
     */
    public String uploadReportFile(Path filePath, String fileName, String contentType) throws IOException {
        byte[] fileBytes = Files.readAllBytes(filePath);
        return uploadReportFile(fileBytes, fileName, contentType);
    }

    /**
     * Sube un archivo de reporte a Supabase Storage desde bytes
     */
    public String uploadReportFile(byte[] fileBytes, String fileName, String contentType) {
        try {
            String uploadUrl = supabaseUrl + "/storage/v1/object/reportes/" + fileName;

            System.out.println("Subiendo archivo a Supabase:");
            System.out.println("URL: " + uploadUrl);
            System.out.println("Archivo: " + fileName);
            System.out.println("Tamaño: " + fileBytes.length + " bytes");
            System.out.println("Content-Type: " + contentType);

            String response = webClient.post()
                    .uri(uploadUrl)
                    .header("Authorization", "Bearer " + supabaseKey)
                    .header("Content-Type", contentType)
                    .header("x-upsert", "true")
                    .body(BodyInserters.fromValue(fileBytes))
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMinutes(5))
                    .block();

            System.out.println("Respuesta de Supabase: " + response);

            // Construir la URL pública
            String publicUrl = supabaseUrl + "/storage/v1/object/public/reportes/" + fileName;
            System.out.println("URL pública generada: " + publicUrl);

            return publicUrl;

        } catch (Exception e) {
            System.err.println("Error al subir archivo a Supabase: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al subir archivo a Supabase: " + e.getMessage(), e);
        }
    }

    /**
     * Elimina un archivo de reporte de Supabase Storage
     */
    public boolean deleteReportFile(String fileName) {
        try {
            String deleteUrl = supabaseUrl + "/storage/v1/object/reportes/" + fileName;

            System.out.println("Eliminando archivo de Supabase: " + deleteUrl);

            String response = webClient.delete()
                    .uri(deleteUrl)
                    .header("Authorization", "Bearer " + supabaseKey)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMinutes(1))
                    .block();

            System.out.println("Archivo eliminado de Supabase: " + response);
            return true;

        } catch (Exception e) {
            System.err.println("Error al eliminar archivo de Supabase: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Obtiene la URL pública de un archivo en Supabase
     */
    public String getPublicUrl(String fileName) {
        return supabaseUrl + "/storage/v1/object/public/reportes/" + fileName;
    }

    /**
     * Genera un nombre único para el archivo
     */
    public String generateUniqueFileName(String reportType, String format, String instalacionNombre) {
        long timestamp = System.currentTimeMillis();
        String extension = format.equals("excel") ? "xlsx" : "pdf";
        
        String fileName = "reporte_" + reportType + "_" + timestamp;
        
        if (instalacionNombre != null && !instalacionNombre.isEmpty()) {
            String instalacionSafe = instalacionNombre.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();
            fileName += "_" + instalacionSafe;
        }
        
        fileName += "." + extension;
        
        return fileName;
    }
}
