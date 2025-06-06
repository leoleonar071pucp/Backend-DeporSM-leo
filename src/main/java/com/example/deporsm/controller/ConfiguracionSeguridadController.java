package com.example.deporsm.controller;

import com.example.deporsm.model.ConfiguracionSeguridad;
import com.example.deporsm.service.ConfiguracionSeguridadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/configuracion/seguridad")
@CrossOrigin(origins = {"*"})
public class ConfiguracionSeguridadController {
    @Autowired
    private ConfiguracionSeguridadService service;

    @GetMapping
    public ResponseEntity<ConfiguracionSeguridad> getConfiguracion() {
        ConfiguracionSeguridad config = service.getConfiguracion();
        if (config == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(config);
    }

    @PutMapping
    public ResponseEntity<ConfiguracionSeguridad> updateConfiguracion(@RequestBody ConfiguracionSeguridad config) {
        ConfiguracionSeguridad updated = service.updateConfiguracion(config);
        return ResponseEntity.ok(updated);
    }
}
