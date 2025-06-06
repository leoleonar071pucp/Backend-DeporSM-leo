package com.example.deporsm.controller;

import com.example.deporsm.model.ConfiguracionGeneral;
import com.example.deporsm.service.ConfiguracionGeneralService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/configuracion/general")
@CrossOrigin(
  origins = {
    "https://deporsm-apiwith-1035693188565.us-central1.run.app",
    "https://frontend-depor-sm-leo.vercel.app",
    "http://localhost:3000"
  },
  allowCredentials = "true"
)
public class ConfiguracionGeneralController {
    @Autowired
    private ConfiguracionGeneralService service;

    /**
     * Obtiene la configuración general del sistema
     * 
     * @return Configuración general actual
     */
    @GetMapping
    public ResponseEntity<ConfiguracionGeneral> getConfiguracion() {
        ConfiguracionGeneral config = service.getConfiguracion();
        return ResponseEntity.ok(config);
    }

    /**
     * Actualiza la configuración general del sistema
     * 
     * @param config Nueva configuración a aplicar
     * @return Configuración general actualizada
     */
    @PutMapping
    public ResponseEntity<ConfiguracionGeneral> updateConfiguracion(
            @RequestBody ConfiguracionGeneral config) {
        
        // Log para depuración
        System.out.println("⚙️ Actualizando configuración general");
        System.out.println("  - Nombre del sitio: " + config.getNombreSitio());
        System.out.println("  - Límite de tiempo cancelación: " + config.getLimiteTiempoCancelacion() + " horas");
        
        ConfiguracionGeneral updated = service.updateConfiguracion(config);
        return ResponseEntity.ok(updated);
    }
}
