package com.example.deporsm.controller;

import com.example.deporsm.dto.AsistenciaCoordinadorDTO;
import com.example.deporsm.dto.AsistenciaCoordinadorRequestDTO;
import com.example.deporsm.dto.AsistenciaCoordinadorResumenDTO;
import com.example.deporsm.service.AsistenciaCoordinadorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.util.List;

@RestController
@RequestMapping("/api/asistencias-coordinadores")
public class AsistenciaCoordinadorController {

    @Autowired
    private AsistenciaCoordinadorService asistenciaCoordinadorService;

    // Obtener todas las asistencias
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINADOR')")
    public ResponseEntity<List<AsistenciaCoordinadorDTO>> obtenerTodasAsistencias() {
        List<AsistenciaCoordinadorDTO> asistencias = asistenciaCoordinadorService.obtenerTodasAsistencias();
        return new ResponseEntity<>(asistencias, HttpStatus.OK);
    }

    // Obtener asistencia por ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINADOR')")
    public ResponseEntity<AsistenciaCoordinadorDTO> obtenerAsistenciaPorId(@PathVariable Integer id) {
        AsistenciaCoordinadorDTO asistencia = asistenciaCoordinadorService.obtenerAsistenciaPorId(id);
        return new ResponseEntity<>(asistencia, HttpStatus.OK);
    }

    // Obtener asistencias por coordinador
    @GetMapping("/coordinador/{coordinadorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINADOR')")
    public ResponseEntity<List<AsistenciaCoordinadorDTO>> obtenerAsistenciasPorCoordinador(@PathVariable Integer coordinadorId) {
        List<AsistenciaCoordinadorDTO> asistencias = asistenciaCoordinadorService.obtenerAsistenciasPorCoordinador(coordinadorId);
        return new ResponseEntity<>(asistencias, HttpStatus.OK);
    }

    // Obtener asistencias por instalación
    @GetMapping("/instalacion/{instalacionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINADOR')")
    public ResponseEntity<List<AsistenciaCoordinadorDTO>> obtenerAsistenciasPorInstalacion(@PathVariable Integer instalacionId) {
        List<AsistenciaCoordinadorDTO> asistencias = asistenciaCoordinadorService.obtenerAsistenciasPorInstalacion(instalacionId);
        return new ResponseEntity<>(asistencias, HttpStatus.OK);
    }

    // Obtener asistencias por fecha
    @GetMapping("/fecha/{fecha}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINADOR')")
    public ResponseEntity<List<AsistenciaCoordinadorDTO>> obtenerAsistenciasPorFecha(@PathVariable Date fecha) {
        List<AsistenciaCoordinadorDTO> asistencias = asistenciaCoordinadorService.obtenerAsistenciasPorFecha(fecha);
        return new ResponseEntity<>(asistencias, HttpStatus.OK);
    }

    // Obtener asistencias por coordinador en un rango de fechas
    @GetMapping("/coordinador/{coordinadorId}/fechas")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINADOR')")
    public ResponseEntity<List<AsistenciaCoordinadorDTO>> obtenerAsistenciasPorCoordinadorYRangoFechas(
            @PathVariable Integer coordinadorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fechaFin) {
        List<AsistenciaCoordinadorDTO> asistencias = asistenciaCoordinadorService
                .obtenerAsistenciasPorCoordinadorYRangoFechas(coordinadorId, fechaInicio, fechaFin);
        return new ResponseEntity<>(asistencias, HttpStatus.OK);
    }

    // Obtener resumen de asistencias por coordinador
    @GetMapping("/resumen/coordinador/{coordinadorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINADOR')")
    public ResponseEntity<AsistenciaCoordinadorResumenDTO> obtenerResumenAsistenciasCoordinador(@PathVariable Integer coordinadorId) {
        AsistenciaCoordinadorResumenDTO resumen = asistenciaCoordinadorService.obtenerResumenAsistenciasCoordinador(coordinadorId);
        return new ResponseEntity<>(resumen, HttpStatus.OK);
    }

    // Endpoint de prueba para verificar deserialización
    @PostMapping("/test")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINADOR')")
    public ResponseEntity<?> testDeserialization(@RequestBody String rawJson) {
        try {
            System.out.println("[DEBUG] JSON recibido: " + rawJson);
            return ResponseEntity.ok("JSON recibido correctamente");
        } catch (Exception e) {
            System.err.println("[ERROR] Error en test: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error en test: " + e.getMessage());
        }
    }

    // Crear nueva asistencia
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINADOR')")
    public ResponseEntity<?> crearAsistencia(@RequestBody AsistenciaCoordinadorRequestDTO requestDTO) {
        try {
            System.out.println("[DEBUG] Datos recibidos en el controlador:");
            System.out.println("  - coordinadorId: " + requestDTO.getCoordinadorId());
            System.out.println("  - instalacionId: " + requestDTO.getInstalacionId());
            System.out.println("  - fecha: " + requestDTO.getFecha());
            System.out.println("  - horaProgramadaInicio: " + requestDTO.getHoraProgramadaInicio());
            System.out.println("  - horaProgramadaFin: " + requestDTO.getHoraProgramadaFin());
            System.out.println("  - horaEntrada: " + requestDTO.getHoraEntrada());
            System.out.println("  - estadoEntrada: " + requestDTO.getEstadoEntrada());
            System.out.println("  - estadoSalida: " + requestDTO.getEstadoSalida());
            System.out.println("  - ubicacion: " + requestDTO.getUbicacion());
            System.out.println("  - notas: " + requestDTO.getNotas());

            AsistenciaCoordinadorDTO asistenciaCreada = asistenciaCoordinadorService.crearAsistencia(requestDTO);
            System.out.println("[DEBUG] Asistencia creada exitosamente: " + asistenciaCreada);
            return new ResponseEntity<>(asistenciaCreada, HttpStatus.CREATED);
        } catch (Exception e) {
            System.err.println("[ERROR] Error al crear asistencia: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al crear asistencia: " + e.getMessage());
        }
    }

    // Actualizar asistencia existente
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<AsistenciaCoordinadorDTO> actualizarAsistencia(
            @PathVariable Integer id,
            @RequestBody AsistenciaCoordinadorRequestDTO requestDTO) {
        AsistenciaCoordinadorDTO asistenciaActualizada = asistenciaCoordinadorService.actualizarAsistencia(id, requestDTO);
        return new ResponseEntity<>(asistenciaActualizada, HttpStatus.OK);
    }

    // Eliminar asistencia
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Void> eliminarAsistencia(@PathVariable Integer id) {
        asistenciaCoordinadorService.eliminarAsistencia(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // Registrar entrada
    @PatchMapping("/{id}/entrada")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINADOR')")
    public ResponseEntity<AsistenciaCoordinadorDTO> registrarEntrada(
            @PathVariable Integer id,
            @RequestBody AsistenciaCoordinadorRequestDTO requestDTO) {
        AsistenciaCoordinadorDTO asistenciaActualizada = asistenciaCoordinadorService.registrarEntrada(id, requestDTO);
        return new ResponseEntity<>(asistenciaActualizada, HttpStatus.OK);
    }

    // Registrar salida
    @PatchMapping("/{id}/salida")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINADOR')")
    public ResponseEntity<AsistenciaCoordinadorDTO> registrarSalida(
            @PathVariable Integer id,
            @RequestBody AsistenciaCoordinadorRequestDTO requestDTO) {
        AsistenciaCoordinadorDTO asistenciaActualizada = asistenciaCoordinadorService.registrarSalida(id, requestDTO);
        return new ResponseEntity<>(asistenciaActualizada, HttpStatus.OK);
    }
}
