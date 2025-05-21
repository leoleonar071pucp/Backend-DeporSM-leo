// src/main/java/com/example/deporsm/controller/ObservacionController.java
package com.example.deporsm.controller;

import com.example.deporsm.dto.ObservacionDTO;
import com.example.deporsm.dto.ObservacionRecienteDTO;
import com.example.deporsm.dto.ObservacionRequestDTO;
import com.example.deporsm.model.Instalacion;
import com.example.deporsm.model.MantenimientoInstalacion;
import com.example.deporsm.model.Observacion;
import com.example.deporsm.model.Usuario;
import com.example.deporsm.repository.InstalacionRepository;
import com.example.deporsm.repository.MantenimientoInstalacionRepository;
import com.example.deporsm.repository.ObservacionRepository;
import com.example.deporsm.repository.UsuarioRepository;
import com.example.deporsm.service.NotificacionService;
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

    @Autowired
    private MantenimientoInstalacionRepository mantenimientoRepository;

    @Autowired
    private NotificacionService notificacionService;

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

    @PutMapping("/{id}/aprobar")
    public ResponseEntity<?> aprobarObservacion(@PathVariable Integer id, @RequestBody(required = false) Map<String, String> feedback) {
        Map<String, Object> response = new HashMap<>();

        try {
            Optional<Observacion> observacionOpt = observacionRepository.findById(id);
            if (observacionOpt.isEmpty()) {
                response.put("mensaje", "La observación no existe");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Observacion observacion = observacionOpt.get();

            // Verificar si la observación ya fue procesada
            if (!"pendiente".equals(observacion.getEstado())) {
                response.put("mensaje", "La observación ya fue procesada anteriormente");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Verificar si la instalación ya tiene un mantenimiento activo
            Instalacion instalacion = observacion.getInstalacion();
            List<MantenimientoInstalacion> mantenimientosActivos = mantenimientoRepository.findMantenimientosActivos(instalacion.getId());

            if (!mantenimientosActivos.isEmpty()) {
                response.put("mensaje", "La instalación ya tiene un mantenimiento programado o en progreso");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Actualizar la observación a estado "en_proceso"
            observacion.setEstado("en_proceso");

            // Guardar comentario de resolución si existe
            if (feedback != null && feedback.containsKey("comentario")) {
                observacion.setComentarioResolucion(feedback.get("comentario"));
            }

            // Marcar la instalación como que requiere mantenimiento
            instalacion.setRequiereMantenimiento(true);
            instalacionRepository.save(instalacion);

            // Guardar la observación actualizada
            observacionRepository.save(observacion);

            // Enviar notificación al coordinador
            notificacionService.crearNotificacion(
                observacion.getUsuario().getId(),
                "Observación aprobada",
                "Su observación sobre " + instalacion.getNombre() + " ha sido aprobada y se programará mantenimiento.",
                "observacion"
            );

            response.put("mensaje", "Observación aprobada con éxito");
            response.put("observacion", observacion);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("mensaje", "Error al aprobar la observación");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/{id}/rechazar")
    public ResponseEntity<?> rechazarObservacion(@PathVariable Integer id, @RequestBody(required = false) Map<String, String> feedback) {
        Map<String, Object> response = new HashMap<>();

        try {
            Optional<Observacion> observacionOpt = observacionRepository.findById(id);
            if (observacionOpt.isEmpty()) {
                response.put("mensaje", "La observación no existe");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Observacion observacion = observacionOpt.get();

            // Verificar si la observación ya fue procesada
            if (!"pendiente".equals(observacion.getEstado())) {
                response.put("mensaje", "La observación ya fue procesada anteriormente");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Actualizar la observación a estado "cancelada"
            observacion.setEstado("cancelada");

            // Guardar comentario de resolución si existe
            if (feedback != null && feedback.containsKey("comentario")) {
                observacion.setComentarioResolucion(feedback.get("comentario"));
            }

            // Guardar la observación actualizada
            observacionRepository.save(observacion);

            // Enviar notificación al coordinador
            notificacionService.crearNotificacion(
                observacion.getUsuario().getId(),
                "Observación rechazada",
                "Su observación sobre " + observacion.getInstalacion().getNombre() + " ha sido rechazada.",
                "observacion"
            );

            response.put("mensaje", "Observación rechazada con éxito");
            response.put("observacion", observacion);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("mensaje", "Error al rechazar la observación");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/{id}/completar")
    public ResponseEntity<?> completarObservacion(@PathVariable Integer id, @RequestBody(required = false) Map<String, String> feedback) {
        Map<String, Object> response = new HashMap<>();

        try {
            Optional<Observacion> observacionOpt = observacionRepository.findById(id);
            if (observacionOpt.isEmpty()) {
                response.put("mensaje", "La observación no existe");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Observacion observacion = observacionOpt.get();

            // Verificar si la observación está en proceso
            if (!"en_proceso".equals(observacion.getEstado())) {
                response.put("mensaje", "La observación no está en proceso");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Actualizar la observación a estado "resuelta"
            observacion.setEstado("resuelta");
            observacion.setFechaResolucion(new Timestamp(System.currentTimeMillis()));

            // Guardar comentario de resolución si existe
            if (feedback != null && feedback.containsKey("comentario")) {
                observacion.setComentarioResolucion(feedback.get("comentario"));
            }

            // Actualizar la instalación
            Instalacion instalacion = observacion.getInstalacion();
            instalacion.setRequiereMantenimiento(false);
            instalacionRepository.save(instalacion);

            // Guardar la observación actualizada
            observacionRepository.save(observacion);

            // Enviar notificación al coordinador
            notificacionService.crearNotificacion(
                observacion.getUsuario().getId(),
                "Observación resuelta",
                "Su observación sobre " + instalacion.getNombre() + " ha sido resuelta.",
                "observacion"
            );

            response.put("mensaje", "Observación completada con éxito");
            response.put("observacion", observacion);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("mensaje", "Error al completar la observación");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Verifica si una instalación requiere mantenimiento basado en observaciones en proceso
     * @param instalacionId ID de la instalación
     * @return Información sobre si la instalación requiere mantenimiento
     */
    @GetMapping("/instalacion/{instalacionId}/requiere-mantenimiento")
    public ResponseEntity<Map<String, Object>> requiereMantenimiento(@PathVariable Integer instalacionId) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Verificar que la instalación existe
            Optional<Instalacion> instalacionOpt = instalacionRepository.findById(instalacionId);
            if (instalacionOpt.isEmpty()) {
                response.put("mensaje", "La instalación no existe");
                response.put("requiereMantenimiento", false);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Instalacion instalacion = instalacionOpt.get();

            // Verificar si la instalación tiene el flag requiereMantenimiento activado
            boolean requiereMantenimiento = instalacion.getRequiereMantenimiento() != null &&
                                           instalacion.getRequiereMantenimiento();

            // Verificar si hay observaciones en proceso para esta instalación
            List<Observacion> observacionesEnProceso = observacionRepository.findByInstalacionIdAndEstado(
                instalacionId, "en_proceso");

            // Si hay observaciones en proceso, la instalación requiere mantenimiento
            if (!observacionesEnProceso.isEmpty()) {
                requiereMantenimiento = true;

                // Si no está marcada ya, actualizar la instalación
                if (!instalacion.getRequiereMantenimiento()) {
                    instalacion.setRequiereMantenimiento(true);
                    instalacionRepository.save(instalacion);
                }
            }

            response.put("requiereMantenimiento", requiereMantenimiento);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("mensaje", "Error al verificar si la instalación requiere mantenimiento");
            response.put("error", e.getMessage());
            response.put("requiereMantenimiento", false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}


