// src/main/java/com/example/deporsm/controller/ObservacionController.java
package com.example.deporsm.controller;

import com.example.deporsm.dto.ObservacionDTO;
import com.example.deporsm.dto.ObservacionRecienteDTO;
import com.example.deporsm.repository.ObservacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/observaciones")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class ObservacionController {

    @Autowired
    private ObservacionRepository observacionRepository;

    @GetMapping("/all")
    public List<ObservacionDTO> listarObservaciones() {
        return observacionRepository.findAllObservacionesDTO();
    }

    @GetMapping("/recientes")
    public List<ObservacionRecienteDTO> listarObservacionesRecientes() {
        return observacionRepository.findObservacionesRecientes();
    }

    @GetMapping("/coordinador/{id}")
    public List<ObservacionDTO> listarObservacionesPorCoordinador(@PathVariable("id") Integer coordinadorId) {
        return observacionRepository.findObservacionesDTOByCoordinadorId(coordinadorId);
    }
}


