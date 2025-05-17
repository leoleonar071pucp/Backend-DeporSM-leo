// src/main/java/com/example/deporsm/controller/ObservacionController.java
package com.example.deporsm.controller;

import com.example.deporsm.dto.ObservacionDTO;
import com.example.deporsm.dto.ObservacionRecienteDTO;
import com.example.deporsm.dto.ObservacionRequestDTO;
import com.example.deporsm.model.Instalacion;
import com.example.deporsm.model.Observacion;
import com.example.deporsm.model.Usuario;
import com.example.deporsm.repository.InstalacionRepository;
import com.example.deporsm.repository.ObservacionRepository;
import com.example.deporsm.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/observaciones")
@CrossOrigin(
  origins = {
    "https://deporsm-apiwith-1035693188565.us-central1.run.app",
    "https://frontend-depor-sm-leo.vercel.app",
    "http://localhost:3000"
  },
  allowCredentials = "true"
)public class ObservacionController {

    @Autowired
    private ObservacionRepository observacionRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private InstalacionRepository instalacionRepository;

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
    
    @PostMapping
    public ResponseEntity<?> crearObservacion(
            @RequestBody ObservacionRequestDTO requestDTO) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validar que exista el usuario
            Optional<Usuario> usuarioOpt = usuarioRepository.findById(requestDTO.getUsuarioId());
            if (usuarioOpt.isEmpty()) {
                response.put("mensaje", "El usuario no existe");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            // Validar que exista la instalación
            Optional<Instalacion> instalacionOpt = instalacionRepository.findById(requestDTO.getInstalacionId());
            if (instalacionOpt.isEmpty()) {
                response.put("mensaje", "La instalación no existe");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
              // Crear la observación
            Observacion observacion = new Observacion();
            observacion.setUsuario(usuarioOpt.get());
            observacion.setInstalacion(instalacionOpt.get());
            observacion.setTitulo(requestDTO.getTitulo());
            observacion.setDescripcion(requestDTO.getDescripcion());
            observacion.setPrioridad(requestDTO.getPrioridad());
            observacion.setEstado("pendiente");
            
            // Manejar URLs de las fotos
            if (requestDTO.getFotos() != null && !requestDTO.getFotos().isEmpty()) {
                String fotosUrlString = String.join(",", requestDTO.getFotos());
                observacion.setFotosUrl(fotosUrlString);
            }
            
            // Guardar coordenadas si están disponibles
            if (requestDTO.getUbicacionLat() != null && requestDTO.getUbicacionLng() != null) {
                String ubicacion = requestDTO.getUbicacionLat() + "," + requestDTO.getUbicacionLng();
                // Aquí podrías guardar las coordenadas en un nuevo campo de la entidad si lo agregas
            }
            
            // Timestamps automáticos
            Timestamp now = new Timestamp(System.currentTimeMillis());
            observacion.setCreatedAt(now);
            observacion.setUpdatedAt(now);
            
            // Guardar en base de datos
            observacion = observacionRepository.save(observacion);
            
            response.put("mensaje", "Observación creada con éxito");
            response.put("observacion", observacion);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            response.put("mensaje", "Error al crear la observación");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}


