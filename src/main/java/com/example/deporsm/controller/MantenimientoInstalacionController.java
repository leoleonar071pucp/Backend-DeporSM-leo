package com.example.deporsm.controller;

import com.example.deporsm.dto.MantenimientoDTO;
import com.example.deporsm.model.MantenimientoInstalacion;
import com.example.deporsm.repository.MantenimientoInstalacionRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/mantenimientos")
@CrossOrigin(origins = "http://localhost:3000")
public class MantenimientoInstalacionController {

    private final MantenimientoInstalacionRepository mantenimientoRepository;

    public MantenimientoInstalacionController(MantenimientoInstalacionRepository mantenimientoRepository) {
        this.mantenimientoRepository = mantenimientoRepository;
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
}
