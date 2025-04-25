package com.example.deporsm.controller;

import com.example.deporsm.model.Instalacion;
import com.example.deporsm.repository.InstalacionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/instalaciones")
@CrossOrigin(origins = "http://localhost:3000")


public class InstalacionesController {

    private final InstalacionRepository repository;

    public InstalacionesController(InstalacionRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Instalacion> listarTodas() {
        return repository.findAll();
    }

    @GetMapping("/buscar")
    public List<Instalacion> buscarPorNombre(@RequestParam String nombre) {
        return repository.findByNombreContainingIgnoreCase(nombre);
    }

    @GetMapping("/ubicacion")
    public List<Instalacion> buscarPorUbicacion(@RequestParam String ubicacion) {
        return repository.buscarPorUbicacion(ubicacion);
    }

    @GetMapping("/tipo")
    public List<Instalacion> filtrarPorTipo(@RequestParam String tipo) {
        return repository.findByTipo(tipo);
    }

    @GetMapping("/activo")
    public List<Instalacion> filtrarPorEstadoActivo(@RequestParam Boolean activo) {
        return repository.findByActivo(activo);
    }

    @GetMapping("/nombre-estado")
    public List<Instalacion> buscarPorNombreYEstado(@RequestParam String nombre, @RequestParam Boolean activo) {
        return repository.findByNombreContainingIgnoreCaseAndActivo(nombre, activo);
    }

    @GetMapping("/tipo-estado")
    public List<Instalacion> buscarPorTipoYEstado(@RequestParam String tipo, @RequestParam Boolean activo) {
        return repository.findByTipoAndActivo(tipo, activo);
    }

    @GetMapping("/autocomplete")
    public List<Instalacion> autocompletarNombreTipo(@RequestParam String query) {
        return repository.autocompleteByNombreOrTipo(query);
    }

    @PostMapping
    public Instalacion crear(@RequestBody Instalacion nueva) {
        return repository.save(nueva);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Instalacion> obtenerPorId(@PathVariable Integer id) {
        return repository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Instalacion> actualizar(@PathVariable Integer id, @RequestBody Instalacion actualizada) {
        return repository.findById(id).map(inst -> {
            inst.setNombre(actualizada.getNombre());
            inst.setDescripcion(actualizada.getDescripcion());
            inst.setUbicacion(actualizada.getUbicacion());
            inst.setTipo(actualizada.getTipo());
            inst.setCapacidad(actualizada.getCapacidad());
            inst.setHorarioApertura(actualizada.getHorarioApertura());
            inst.setHorarioCierre(actualizada.getHorarioCierre());
            inst.setImagenUrl(actualizada.getImagenUrl());
            inst.setActivo(actualizada.getActivo());
            return ResponseEntity.ok(repository.save(inst));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
