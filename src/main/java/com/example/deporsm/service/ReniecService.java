package com.example.deporsm.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;

@Service
public class ReniecService {

    @Value("${reniec.api.url}")
    private String reniecApiUrl;

    @Value("${reniec.api.token}")
    private String reniecApiToken;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public ReniecService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Verifica un DNI con la API de RENIEC
     * @param dni DNI a verificar (8 dígitos)
     * @return ReniecResponse con los datos de la persona o información del error
     */
    public ReniecResponse verificarDni(String dni) {
        System.out.println("[RENIEC] Verificando DNI: " + dni);

        try {
            // Validar formato del DNI
            if (dni == null || !dni.matches("\\d{8}")) {
                return ReniecResponse.error("DNI debe tener exactamente 8 dígitos");
            }

            // Preparar headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + reniecApiToken);
            headers.set("Content-Type", "application/json");
            headers.set("Accept", "application/json");

            // Crear la URL completa
            String url = reniecApiUrl + "?numero=" + dni;

            System.out.println("[RENIEC] Llamando a URL: " + url);

            // Hacer la petición
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
            );

            System.out.println("[RENIEC] Respuesta recibida: " + response.getStatusCode());

            // Procesar la respuesta
            if (response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                System.out.println("[RENIEC] Cuerpo de respuesta: " + responseBody);

                JsonNode jsonNode = objectMapper.readTree(responseBody);

                // Verificar si la respuesta contiene datos válidos
                if (jsonNode.has("nombres") && jsonNode.has("apellidoPaterno") && jsonNode.has("apellidoMaterno")) {
                    String nombres = formatearNombre(jsonNode.get("nombres").asText());
                    String apellidoPaterno = formatearNombre(jsonNode.get("apellidoPaterno").asText());
                    String apellidoMaterno = formatearNombre(jsonNode.get("apellidoMaterno").asText());

                    // Construir nombre completo
                    String nombreCompleto = nombres + " " + apellidoPaterno + " " + apellidoMaterno;

                    System.out.println("[RENIEC] DNI verificado exitosamente: " + nombreCompleto);
                    return ReniecResponse.success(dni, nombreCompleto, nombres, apellidoPaterno, apellidoMaterno);
                } else {
                    System.out.println("[RENIEC] Respuesta no contiene datos esperados");
                    return ReniecResponse.error("DNI no encontrado en RENIEC");
                }
            } else {
                System.out.println("[RENIEC] Error en respuesta: " + response.getStatusCode());
                return ReniecResponse.error("Error al consultar RENIEC: " + response.getStatusCode());
            }

        } catch (HttpClientErrorException e) {
            System.out.println("[RENIEC] Error HTTP: " + e.getStatusCode() + " - " + e.getMessage());

            if (e.getStatusCode().value() == 404) {
                return ReniecResponse.error("DNI no encontrado en RENIEC");
            } else if (e.getStatusCode().value() == 401) {
                return ReniecResponse.error("Token de API inválido");
            } else if (e.getStatusCode().value() == 429) {
                return ReniecResponse.error("Límite de consultas excedido. Intente más tarde");
            } else {
                return ReniecResponse.error("Error al consultar RENIEC: " + e.getStatusCode());
            }

        } catch (ResourceAccessException e) {
            System.out.println("[RENIEC] Error de conexión: " + e.getMessage());
            return ReniecResponse.error("Error de conexión con RENIEC. Verifique su conexión a internet");

        } catch (Exception e) {
            System.out.println("[RENIEC] Error inesperado: " + e.getMessage());
            e.printStackTrace();
            return ReniecResponse.error("Error interno al verificar DNI");
        }
    }

    /**
     * Formatea un nombre para que tenga solo las primeras letras en mayúsculas
     * @param nombre Nombre en mayúsculas o cualquier formato
     * @return Nombre formateado con primera letra de cada palabra en mayúscula
     */
    private String formatearNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return nombre;
        }

        // Convertir a minúsculas y luego capitalizar cada palabra
        String[] palabras = nombre.toLowerCase().trim().split("\\s+");
        StringBuilder nombreFormateado = new StringBuilder();

        for (int i = 0; i < palabras.length; i++) {
            if (i > 0) {
                nombreFormateado.append(" ");
            }

            String palabra = palabras[i];
            if (!palabra.isEmpty()) {
                // Capitalizar primera letra y mantener el resto en minúsculas
                nombreFormateado.append(Character.toUpperCase(palabra.charAt(0)));
                if (palabra.length() > 1) {
                    nombreFormateado.append(palabra.substring(1));
                }
            }
        }

        return nombreFormateado.toString();
    }

    /**
     * Clase para encapsular la respuesta de RENIEC
     */
    public static class ReniecResponse {
        private boolean success;
        private String dni;
        private String nombreCompleto;
        private String nombres;
        private String apellidoPaterno;
        private String apellidoMaterno;
        private String errorMessage;

        // Constructor privado
        private ReniecResponse() {}

        // Método estático para crear respuesta exitosa
        public static ReniecResponse success(String dni, String nombreCompleto, String nombres, String apellidoPaterno, String apellidoMaterno) {
            ReniecResponse response = new ReniecResponse();
            response.success = true;
            response.dni = dni;
            response.nombreCompleto = nombreCompleto;
            response.nombres = nombres;
            response.apellidoPaterno = apellidoPaterno;
            response.apellidoMaterno = apellidoMaterno;
            return response;
        }

        // Método estático para crear respuesta de error
        public static ReniecResponse error(String errorMessage) {
            ReniecResponse response = new ReniecResponse();
            response.success = false;
            response.errorMessage = errorMessage;
            return response;
        }

        // Getters
        public boolean isSuccess() { return success; }
        public String getDni() { return dni; }
        public String getNombreCompleto() { return nombreCompleto; }
        public String getNombres() { return nombres; }
        public String getApellidoPaterno() { return apellidoPaterno; }
        public String getApellidoMaterno() { return apellidoMaterno; }
        public String getErrorMessage() { return errorMessage; }

        // Método para convertir a Map (útil para respuestas JSON)
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("success", success);
            if (success) {
                map.put("dni", dni);
                map.put("nombreCompleto", nombreCompleto);
                map.put("nombres", nombres);
                map.put("apellidoPaterno", apellidoPaterno);
                map.put("apellidoMaterno", apellidoMaterno);
            } else {
                map.put("error", errorMessage);
            }
            return map;
        }
    }
}
