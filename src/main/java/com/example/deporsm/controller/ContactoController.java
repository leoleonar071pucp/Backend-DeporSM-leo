package com.example.deporsm.controller;

import com.example.deporsm.dto.ContactoDTO;
import com.example.deporsm.service.ContactoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para manejar los mensajes de contacto
 */
@RestController
@RequestMapping("/api/contacto")
@CrossOrigin(
  origins = {
    "https://deporsm-apiwith-1035693188565.us-central1.run.app",
    "https://frontend-depor-sm-leo.vercel.app",
    "http://localhost:3000"
  },
  allowCredentials = "true"
)
public class ContactoController {

    @Autowired
    private ContactoService contactoService;

    /**
     * Endpoint para enviar un mensaje de contacto
     *
     * @param contactoDTO Datos del mensaje de contacto
     * @return Respuesta con el resultado de la operación
     */
    @PostMapping("/enviar")
    public ResponseEntity<?> enviarMensaje(@RequestBody ContactoDTO contactoDTO) {
        try {
            System.out.println("[INFO] Recibida solicitud de contacto de: " + contactoDTO.getEmail());

            // Validar datos
            if (contactoDTO.getNombre() == null || contactoDTO.getNombre().trim().isEmpty()) {
                System.out.println("[ERROR] Validación fallida: Nombre requerido");
                return ResponseEntity.badRequest().body("El nombre es requerido");
            }

            if (contactoDTO.getEmail() == null || contactoDTO.getEmail().trim().isEmpty()) {
                System.out.println("[ERROR] Validación fallida: Email requerido");
                return ResponseEntity.badRequest().body("El correo electrónico es requerido");
            }

            if (contactoDTO.getAsunto() == null || contactoDTO.getAsunto().trim().isEmpty()) {
                System.out.println("[ERROR] Validación fallida: Asunto requerido");
                return ResponseEntity.badRequest().body("El asunto es requerido");
            }

            if (contactoDTO.getMensaje() == null || contactoDTO.getMensaje().trim().isEmpty()) {
                System.out.println("[ERROR] Validación fallida: Mensaje requerido");
                return ResponseEntity.badRequest().body("El mensaje es requerido");
            }

            System.out.println("[INFO] Validación exitosa, procesando mensaje de contacto");

            // Procesar mensaje
            contactoService.procesarMensajeContacto(contactoDTO);

            // Respuesta exitosa
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Mensaje enviado correctamente");

            System.out.println("[INFO] Mensaje de contacto procesado exitosamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("[ERROR] Error al procesar mensaje de contacto: " + e.getMessage());
            e.printStackTrace();

            // Crear respuesta de error con detalles
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al enviar el mensaje: " + e.getMessage());
            errorResponse.put("error", e.getClass().getName());

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
