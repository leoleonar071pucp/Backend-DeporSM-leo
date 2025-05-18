package com.example.deporsm.controller;

import com.example.deporsm.dto.BloqueoTemporalDTO;
import com.example.deporsm.service.BloqueoTemporalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bloqueos-temporales")
@CrossOrigin(
  origins = {
    "https://deporsm-apiwith-1035693188565.us-central1.run.app",
    "https://frontend-depor-sm-leo.vercel.app",
    "http://localhost:3000"
  },
  allowCredentials = "true"
)
public class BloqueoTemporalController {

    @Autowired
    private BloqueoTemporalService bloqueoTemporalService;

    /**
     * Crea un bloqueo temporal para un horario específico
     */
    @PostMapping
    public ResponseEntity<?> crearBloqueoTemporal(@RequestBody BloqueoTemporalDTO bloqueoDTO) {
        System.out.println("Recibida solicitud para crear bloqueo temporal: " + bloqueoDTO.getInstalacionId() +
                          ", fecha: " + bloqueoDTO.getFecha() +
                          ", hora: " + bloqueoDTO.getHoraInicio() + " - " + bloqueoDTO.getHoraFin());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
            || authentication.getPrincipal().equals("anonymousUser")) {
            System.err.println("Error: Usuario no autenticado");
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        String email = authentication.getName();
        System.out.println("Usuario autenticado: " + email);

        try {
            BloqueoTemporalDTO bloqueoCreado = bloqueoTemporalService.crearBloqueoTemporal(email, bloqueoDTO);
            System.out.println("Bloqueo temporal creado con éxito. Token: " + bloqueoCreado.getToken());
            return ResponseEntity.ok(bloqueoCreado);
        } catch (Exception e) {
            System.err.println("Error al crear bloqueo temporal: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error al crear bloqueo temporal: " + e.getMessage());
        }
    }

    /**
     * Libera un bloqueo temporal usando su token
     */
    @DeleteMapping("/{token}")
    public ResponseEntity<?> liberarBloqueo(@PathVariable String token) {
        System.out.println("Recibida solicitud para liberar bloqueo temporal con token: " + token);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
            || authentication.getPrincipal().equals("anonymousUser")) {
            System.err.println("Error: Usuario no autenticado al intentar liberar bloqueo");
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        String email = authentication.getName();
        System.out.println("Usuario autenticado: " + email);

        try {
            bloqueoTemporalService.liberarBloqueo(token);
            System.out.println("Bloqueo temporal liberado con éxito. Token: " + token);
            return ResponseEntity.ok("Bloqueo liberado correctamente");
        } catch (Exception e) {
            System.err.println("Error al liberar bloqueo temporal: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error al liberar bloqueo: " + e.getMessage());
        }
    }

    /**
     * Verifica si un horario está disponible (no reservado y no bloqueado temporalmente)
     */
    @GetMapping("/verificar-disponibilidad")
    public ResponseEntity<?> verificarDisponibilidad(
            @RequestParam Integer instalacionId,
            @RequestParam String fecha,
            @RequestParam String horaInicio,
            @RequestParam String horaFin) {
        System.out.println("Verificando disponibilidad para instalación: " + instalacionId +
                          ", fecha: " + fecha +
                          ", hora: " + horaInicio + " - " + horaFin);
        try {
            java.sql.Date sqlFecha = java.sql.Date.valueOf(fecha);
            java.sql.Time sqlHoraInicio = java.sql.Time.valueOf(horaInicio + ":00");
            java.sql.Time sqlHoraFin = java.sql.Time.valueOf(horaFin + ":00");

            System.out.println("Parámetros convertidos: fecha=" + sqlFecha +
                              ", horaInicio=" + sqlHoraInicio +
                              ", horaFin=" + sqlHoraFin);

            boolean disponible = bloqueoTemporalService.verificarDisponibilidadHorario(
                    instalacionId, sqlFecha, sqlHoraInicio, sqlHoraFin);

            System.out.println("Resultado de verificación de disponibilidad: " + disponible);
            return ResponseEntity.ok(disponible);
        } catch (Exception e) {
            System.err.println("Error al verificar disponibilidad: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error al verificar disponibilidad: " + e.getMessage());
        }
    }
}
