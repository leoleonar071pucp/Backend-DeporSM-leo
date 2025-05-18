package com.example.deporsm.controller;

import com.example.deporsm.dto.CrearNotificacionDTO;
import com.example.deporsm.dto.NotificacionDTO;
import com.example.deporsm.model.Usuario;
import com.example.deporsm.repository.UsuarioRepository;
import com.example.deporsm.service.NotificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/notificaciones")
@CrossOrigin(
  origins = {
    "https://deporsm-apiwith-1035693188565.us-central1.run.app",
    "https://frontend-depor-sm-leo.vercel.app",
    "http://localhost:3000"
  },
  allowCredentials = "true"
)
public class NotificacionController {

    @Autowired
    private NotificacionService notificacionService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Obtiene todas las notificaciones del usuario autenticado
     * @return Lista de notificaciones
     */
    @GetMapping("")
    public ResponseEntity<?> obtenerNotificaciones() {
        Integer usuarioId = obtenerUsuarioIdAutenticado();
        if (usuarioId == null) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        List<NotificacionDTO> notificaciones = notificacionService.obtenerNotificacionesPorUsuario(usuarioId);
        return ResponseEntity.ok(notificaciones);
    }

    /**
     * Obtiene las notificaciones no leídas del usuario autenticado
     * @return Lista de notificaciones no leídas
     */
    @GetMapping("/no-leidas")
    public ResponseEntity<?> obtenerNotificacionesNoLeidas() {
        Integer usuarioId = obtenerUsuarioIdAutenticado();
        if (usuarioId == null) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        List<NotificacionDTO> notificaciones = notificacionService.obtenerNotificacionesNoLeidasPorUsuario(usuarioId);
        return ResponseEntity.ok(notificaciones);
    }

    /**
     * Obtiene las notificaciones leídas del usuario autenticado
     * @return Lista de notificaciones leídas
     */
    @GetMapping("/leidas")
    public ResponseEntity<?> obtenerNotificacionesLeidas() {
        Integer usuarioId = obtenerUsuarioIdAutenticado();
        if (usuarioId == null) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        List<NotificacionDTO> notificaciones = notificacionService.obtenerNotificacionesLeidasPorUsuario(usuarioId);
        return ResponseEntity.ok(notificaciones);
    }

    /**
     * Marca una notificación como leída
     * @param notificacionId ID de la notificación
     * @return Respuesta con el resultado
     */
    @PutMapping("/{notificacionId}/leer")
    public ResponseEntity<?> marcarComoLeida(@PathVariable Integer notificacionId) {
        Integer usuarioId = obtenerUsuarioIdAutenticado();
        if (usuarioId == null) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        boolean resultado = notificacionService.marcarComoLeida(notificacionId, usuarioId);

        if (resultado) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Notificación marcada como leída");
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body("No se pudo marcar la notificación como leída");
        }
    }

    /**
     * Marca todas las notificaciones como leídas
     * @return Respuesta con el resultado
     */
    @PutMapping("/marcar-todas-leidas")
    public ResponseEntity<?> marcarTodasComoLeidas() {
        Integer usuarioId = obtenerUsuarioIdAutenticado();
        if (usuarioId == null) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        notificacionService.marcarTodasComoLeidas(usuarioId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Todas las notificaciones marcadas como leídas");
        return ResponseEntity.ok(response);
    }

    /**
     * Elimina una notificación
     * @param notificacionId ID de la notificación
     * @return Respuesta con el resultado
     */
    @DeleteMapping("/{notificacionId}")
    public ResponseEntity<?> eliminarNotificacion(@PathVariable Integer notificacionId) {
        Integer usuarioId = obtenerUsuarioIdAutenticado();
        if (usuarioId == null) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        boolean resultado = notificacionService.eliminarNotificacion(notificacionId, usuarioId);

        if (resultado) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Notificación eliminada");
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body("No se pudo eliminar la notificación");
        }
    }

    /**
     * Elimina todas las notificaciones leídas
     * @return Respuesta con el resultado
     */
    @DeleteMapping("/eliminar-leidas")
    public ResponseEntity<?> eliminarTodasLasLeidas() {
        Integer usuarioId = obtenerUsuarioIdAutenticado();
        if (usuarioId == null) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        notificacionService.eliminarTodasLasLeidas(usuarioId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Todas las notificaciones leídas han sido eliminadas");
        return ResponseEntity.ok(response);
    }

    /**
     * Crea una notificación de prueba para el usuario autenticado
     * @return Respuesta con el resultado
     */
    @PostMapping("/test")
    public ResponseEntity<?> crearNotificacionPrueba() {
        Integer usuarioId = obtenerUsuarioIdAutenticado();
        if (usuarioId == null) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        NotificacionDTO notificacion = notificacionService.crearNotificacion(
            usuarioId,
            "Notificación de prueba",
            "Esta es una notificación de prueba creada desde el backend",
            "info",
            "test",
            null
        );

        if (notificacion != null) {
            return ResponseEntity.ok(notificacion);
        } else {
            return ResponseEntity.badRequest().body("No se pudo crear la notificación de prueba");
        }
    }

    /**
     * Crea una nueva notificación para el usuario autenticado
     * @param notificacionDTO Datos de la notificación
     * @return La notificación creada
     */
    @PostMapping("/crear")
    public ResponseEntity<?> crearNotificacion(@RequestBody CrearNotificacionDTO notificacionDTO) {
        Integer usuarioId = obtenerUsuarioIdAutenticado();
        if (usuarioId == null) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        NotificacionDTO notificacion = notificacionService.crearNotificacion(
            usuarioId,
            notificacionDTO.getTitulo(),
            notificacionDTO.getMensaje(),
            notificacionDTO.getTipo(),
            notificacionDTO.getCategoria(),
            notificacionDTO.getFeedback()
        );

        if (notificacion != null) {
            return ResponseEntity.ok(notificacion);
        } else {
            return ResponseEntity.badRequest().body("No se pudo crear la notificación");
        }
    }

    /**
     * Obtiene el ID del usuario autenticado
     * @return ID del usuario o null si no está autenticado
     */
    private Integer obtenerUsuarioIdAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
            || authentication.getPrincipal().equals("anonymousUser")) {
            return null;
        }

        // El principal es el email del usuario
        String email = authentication.getName();

        try {
            // Buscar el usuario por email
            Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
            if (usuarioOpt.isPresent()) {
                return usuarioOpt.get().getId();
            }
        } catch (Exception e) {
            System.err.println("Error al obtener usuario por email: " + e.getMessage());
        }

        return null;
    }
}
