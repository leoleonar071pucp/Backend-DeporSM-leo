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
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class MantenimientoInstalacionController {

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
    public List<MantenimientoInstalacion> obtenerActivos(@RequestParam("fechaActual")
                                                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaActual) {
        return mantenimientoRepository.findActivos(fechaActual);
    }

    // Mantenimientos programados: fecha futura
    @GetMapping("/programados")
    public List<MantenimientoInstalacion> obtenerProgramados(@RequestParam("fechaActual")
                                                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaActual) {
        return mantenimientoRepository.findProgramados(fechaActual);
    }

    // Mantenimientos históricos: ya finalizados
    @GetMapping("/historial")
    public List<MantenimientoInstalacion> obtenerHistorial(@RequestParam("fechaActual")
                                                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaActual) {
        return mantenimientoRepository.findFinalizados(fechaActual);
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











}
