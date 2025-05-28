package com.example.deporsm.controller;

import com.example.deporsm.dto.CoordinadorAsignacionDTO;
import com.example.deporsm.model.CoordinadorInstalacion;
import com.example.deporsm.service.CoordinadorInstalacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coordinador-instalaciones")
@CrossOrigin(
    origins = {
        "https://deporsm-apiwith-1035693188565.us-central1.run.app",
        "https://frontend-depor-sm-leo.vercel.app",
        "http://localhost:3000"
    },
    allowCredentials = "true"
)
public class CoordinadorInstalacionController {

    @Autowired
    private CoordinadorInstalacionService coordinadorInstalacionService;

    /**
     * Asigna instalaciones y horarios a un coordinador
     */
    @PostMapping("/asignar")
    public ResponseEntity<?> asignarInstalacionesYHorarios(@RequestBody CoordinadorAsignacionDTO asignacionDTO) {
        try {
            coordinadorInstalacionService.asignarInstalacionesYHorarios(asignacionDTO);
            return ResponseEntity.ok().body("Instalaciones y horarios asignados exitosamente");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al asignar instalaciones: " + e.getMessage());
        }
    }

    /**
     * Obtiene las asignaciones de un coordinador
     */
    @GetMapping("/coordinador/{coordinadorId}")
    public ResponseEntity<List<CoordinadorInstalacion>> obtenerAsignacionesPorCoordinador(
            @PathVariable Integer coordinadorId) {
        try {
            List<CoordinadorInstalacion> asignaciones = 
                coordinadorInstalacionService.obtenerAsignacionesPorCoordinador(coordinadorId);
            return ResponseEntity.ok(asignaciones);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Elimina todas las asignaciones de un coordinador
     */
    @DeleteMapping("/coordinador/{coordinadorId}")
    public ResponseEntity<?> eliminarAsignacionesCoordinador(@PathVariable Integer coordinadorId) {
        try {
            coordinadorInstalacionService.eliminarAsignacionesCoordinador(coordinadorId);
            return ResponseEntity.ok().body("Asignaciones eliminadas exitosamente");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al eliminar asignaciones: " + e.getMessage());
        }
    }
}
