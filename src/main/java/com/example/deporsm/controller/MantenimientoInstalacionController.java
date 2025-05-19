package com.example.deporsm.controller;

import com.example.deporsm.dto.MantenimientoDTO;
import com.example.deporsm.model.Instalacion;
import com.example.deporsm.model.MantenimientoInstalacion;
import com.example.deporsm.model.Usuario;
import com.example.deporsm.repository.MantenimientoInstalacionRepository;
import com.example.deporsm.repository.InstalacionRepository;
import com.example.deporsm.repository.UsuarioRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/mantenimientos")
@CrossOrigin(
  origins = {
    "https://deporsm-apiwith-1035693188565.us-central1.run.app",
    "https://frontend-depor-sm-leo.vercel.app",
    "http://localhost:3000"
  },
  allowCredentials = "true"
)public class MantenimientoInstalacionController {

    private final MantenimientoInstalacionRepository mantenimientoRepository;
    private final InstalacionRepository instalacionRepository;
    private final UsuarioRepository usuarioRepository;

    public MantenimientoInstalacionController(MantenimientoInstalacionRepository mantenimientoRepository
            , InstalacionRepository instalacionRepository
            , UsuarioRepository usuarioRepository) {
        this.mantenimientoRepository = mantenimientoRepository;
        this.instalacionRepository = instalacionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // Filtro combinado por texto, estado, tipo e instalación
    @GetMapping("/filtrar")
    public ResponseEntity<List<MantenimientoDTO>> filtrar(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) Integer instalacionId
    ) {
        return ResponseEntity.ok(mantenimientoRepository.filtrarPorCriterios(texto, instalacionId));
    }


    // Mantenimientos activos: fecha actual dentro del rango
    @GetMapping("/activos")
    public ResponseEntity<List<MantenimientoDTO>> obtenerActivos(
            @RequestParam(value = "fechaActual", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaActual) {

        // Si no se proporciona fecha, usar la fecha actual
        if (fechaActual == null) {
            fechaActual = LocalDateTime.now();
        }

        List<MantenimientoInstalacion> mantenimientos = mantenimientoRepository.findActivos(fechaActual);
        List<MantenimientoDTO> dtos = mantenimientos.stream()
            .map(m -> new MantenimientoDTO(
                m.getId(),
                m.getMotivo(),
                m.getDescripcion(),
                m.getFechaInicio(),
                m.getFechaFin(),
                m.getEstado(),
                m.getInstalacion().getNombre(),
                m.getInstalacion().getUbicacion()
            ))
            .toList();

        return ResponseEntity.ok(dtos);
    }

    // Mantenimientos programados: fecha futura
    @GetMapping("/programados")
    public ResponseEntity<List<MantenimientoDTO>> obtenerProgramados(
            @RequestParam(value = "fechaActual", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaActual) {

        // Si no se proporciona fecha, usar la fecha actual
        if (fechaActual == null) {
            fechaActual = LocalDateTime.now();
        }

        List<MantenimientoInstalacion> mantenimientos = mantenimientoRepository.findProgramados(fechaActual);
        List<MantenimientoDTO> dtos = mantenimientos.stream()
            .map(m -> new MantenimientoDTO(
                m.getId(),
                m.getMotivo(),
                m.getDescripcion(),
                m.getFechaInicio(),
                m.getFechaFin(),
                m.getEstado(),
                m.getInstalacion().getNombre(),
                m.getInstalacion().getUbicacion()
            ))
            .toList();

        return ResponseEntity.ok(dtos);
    }

    // Mantenimientos históricos: ya finalizados
    @GetMapping("/historial")
    public ResponseEntity<List<MantenimientoDTO>> obtenerHistorial(
            @RequestParam(value = "fechaActual", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaActual) {

        // Si no se proporciona fecha, usar la fecha actual
        if (fechaActual == null) {
            fechaActual = LocalDateTime.now();
        }

        List<MantenimientoInstalacion> mantenimientos = mantenimientoRepository.findFinalizados(fechaActual);
        List<MantenimientoDTO> dtos = mantenimientos.stream()
            .map(m -> new MantenimientoDTO(
                m.getId(),
                m.getMotivo(),
                m.getDescripcion(),
                m.getFechaInicio(),
                m.getFechaFin(),
                m.getEstado(),
                m.getInstalacion().getNombre(),
                m.getInstalacion().getUbicacion()
            ))
            .toList();

        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    public ResponseEntity<MantenimientoInstalacion> crearMantenimiento(@RequestBody MantenimientoInstalacion nuevo) {
        if (nuevo.getInstalacion() == null || nuevo.getInstalacion().getId() == null) {
            return ResponseEntity.badRequest().body(null);
        }

        // Buscar instalación
        Instalacion instalacion = instalacionRepository.findById(nuevo.getInstalacion().getId())
                .orElse(null);
        if (instalacion == null) {
            return ResponseEntity.badRequest().body(null);
        }

        nuevo.setInstalacion(instalacion);
        nuevo.setCreatedAt(LocalDateTime.now());
        nuevo.setUpdatedAt(LocalDateTime.now());

        MantenimientoInstalacion guardado = mantenimientoRepository.save(nuevo);
        return ResponseEntity.ok(guardado);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MantenimientoInstalacion> obtenerDetallePorId(@PathVariable Integer id) {
        return mantenimientoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarMantenimiento(@PathVariable Integer id) {
        return mantenimientoRepository.findById(id).map(mantenimiento -> {
            mantenimientoRepository.delete(mantenimiento);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Cancela un mantenimiento programado
     * @param id ID del mantenimiento a cancelar
     * @return ResponseEntity con el resultado de la operación
     */
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarMantenimiento(@PathVariable Integer id) {
        return mantenimientoRepository.findById(id).map(mantenimiento -> {
            // Verificar que el mantenimiento esté en estado programado
            if (mantenimiento.getEstado() == null || "programado".equals(mantenimiento.getEstado())) {
                mantenimiento.setEstado("cancelado");
                mantenimiento.setUpdatedAt(LocalDateTime.now());
                mantenimientoRepository.save(mantenimiento);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                    .body("Solo se pueden cancelar mantenimientos en estado 'programado'");
            }
        }).orElse(ResponseEntity.notFound().build());
    }
}
