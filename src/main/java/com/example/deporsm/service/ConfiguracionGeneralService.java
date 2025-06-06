package com.example.deporsm.service;

import com.example.deporsm.model.ConfiguracionGeneral;
import com.example.deporsm.repository.ConfiguracionGeneralRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ConfiguracionGeneralService {
    @Autowired
    private ConfiguracionGeneralRepository repository;

    /**
     * Obtiene la configuración general del sistema.
     * Si no existe, crea una con valores por defecto.
     * 
     * @return La configuración general del sistema
     */
    public ConfiguracionGeneral getConfiguracion() {
        // Siempre se usa el ID=1 como registro único de configuración
        return repository.findById(1).orElseGet(() -> {
            // Si no existe, crear una configuración con valores por defecto
            ConfiguracionGeneral config = new ConfiguracionGeneral();
            config.setId(1);
            return repository.save(config);
        });
    }

    /**
     * Actualiza la configuración general del sistema.
     * 
     * @param config La nueva configuración a aplicar
     * @return La configuración actualizada
     */
    public ConfiguracionGeneral updateConfiguracion(ConfiguracionGeneral config) {
        // Siempre actualizamos el ID=1 por seguridad
        config.setId(1);
        
        // Actualizar la fecha de modificación
        config.setUpdatedAt(LocalDateTime.now());
        
        return repository.save(config);
    }
}
