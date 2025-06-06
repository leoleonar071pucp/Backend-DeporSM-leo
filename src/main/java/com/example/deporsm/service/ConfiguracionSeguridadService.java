package com.example.deporsm.service;

import com.example.deporsm.model.ConfiguracionSeguridad;
import com.example.deporsm.repository.ConfiguracionSeguridadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ConfiguracionSeguridadService {
    @Autowired
    private ConfiguracionSeguridadRepository repository;

    public ConfiguracionSeguridad getConfiguracion() {
        // Solo hay un registro, id=1
        return repository.findById(1).orElse(null);
    }

    public ConfiguracionSeguridad updateConfiguracion(ConfiguracionSeguridad config) {
        config.setId(1); // Forzar id=1
        return repository.save(config);
    }
}
