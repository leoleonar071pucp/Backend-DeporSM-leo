// src/main/java/com/example/deporsm/controller/ObservacionController.java
package com.example.deporsm.controller;

import com.example.deporsm.dto.ObservacionDTO;
import com.example.deporsm.repository.ObservacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/observaciones")
public class ObservacionController {

    @Autowired
    private ObservacionRepository observacionRepository;

    @GetMapping("/all")
    public List<ObservacionDTO> listarObservaciones() {
        return observacionRepository.findAllObservacionesDTO();
    }
}
