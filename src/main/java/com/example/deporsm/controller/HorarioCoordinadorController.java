package com.example.deporsm.controller;

import com.example.deporsm.dto.HorarioCoordinadorDTO;
import com.example.deporsm.service.HorarioCoordinadorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/horarios-coordinador")
@CrossOrigin(
  origins = {
    "https://deporsm-apiwith-1035693188565.us-central1.run.app",
    "https://frontend-depor-sm-leo.vercel.app",
    "http://localhost:3000"
  },
  allowCredentials = "true"
)public class HorarioCoordinadorController {

    @Autowired
    private HorarioCoordinadorService horarioCoordinadorService;    
      @GetMapping("/coordinador/{usuarioId}")
    public ResponseEntity<List<HorarioCoordinadorDTO>> getHorariosByCoordinadorId(@PathVariable Integer usuarioId) {
        try {
            System.out.println("==================== PETICIÓN DE HORARIOS ====================");
            System.out.println("Recibida solicitud de horarios para el usuario ID: " + usuarioId);
            System.out.println("Origen de la solicitud (Thread): " + Thread.currentThread().getName());
            
            if (usuarioId == null || usuarioId <= 0) {
                System.err.println("ID de usuario inválido: " + usuarioId);
                return ResponseEntity.badRequest().body(null);
            }
              List<HorarioCoordinadorDTO> horarios = horarioCoordinadorService.getHorariosByCoordinadorId(usuarioId);
            System.out.println("Horarios encontrados: " + horarios.size());
            
            // Imprimir detalles de cada horario para depuración
            for (HorarioCoordinadorDTO h : horarios) {
                System.out.println("Horario ID: " + h.getId() + 
                                   ", Día: '" + h.getDiaSemana() + "'" +
                                   ", Hora: " + h.getHoraInicio() + "-" + h.getHoraFin() +
                                   ", Instalación: " + h.getInstalacionNombre() + " (ID: " + h.getInstalacionId() + ")");
            }
              if (horarios.isEmpty()) {
                System.out.println("ADVERTENCIA: No se encontraron horarios para el coordinador ID: " + usuarioId);
            }
            
            return ResponseEntity.ok()
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .header("Expires", "0")
                .body(horarios);        } catch (Exception e) {
            System.err.println("Error al obtener horarios: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                .header("X-Error-Message", "Error interno al procesar la solicitud")
                .build();
        }
    }
      @GetMapping("/coordinador/{usuarioId}/instalacion/{instalacionId}")
    public ResponseEntity<List<HorarioCoordinadorDTO>> getHorariosByCoordinadorIdAndInstalacionId(
            @PathVariable Integer usuarioId, 
            @PathVariable Integer instalacionId) {
        try {
            System.out.println("Recibida solicitud de horarios para el usuario ID: " + usuarioId + " e instalación ID: " + instalacionId);
            
            if (usuarioId == null || usuarioId <= 0 || instalacionId == null || instalacionId <= 0) {
                System.err.println("ID de usuario o instalación inválidos");
                return ResponseEntity.badRequest().build();
            }
            
            List<HorarioCoordinadorDTO> horarios = 
                horarioCoordinadorService.getHorariosByCoordinadorIdAndInstalacionId(usuarioId, instalacionId);
            
            System.out.println("Horarios encontrados para instalación: " + horarios.size());
            
            return ResponseEntity.ok()
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .header("Expires", "0")
                .body(horarios);
        } catch (Exception e) {
            System.err.println("Error al obtener horarios por instalación: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
