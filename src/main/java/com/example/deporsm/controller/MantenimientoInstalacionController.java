package com.example.deporsm.controller;

import com.example.deporsm.dto.MantenimientoDTO;
import com.example.deporsm.dto.MantenimientoInfoDTO;
import com.example.deporsm.dto.MantenimientoRequestDTO;
import com.example.deporsm.model.Instalacion;
import com.example.deporsm.model.MantenimientoInstalacion;
import com.example.deporsm.repository.MantenimientoInstalacionRepository;
import com.example.deporsm.repository.InstalacionRepository;
import com.example.deporsm.service.MantenimientoService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/mantenimientos")
@CrossOrigin(
  origins = {
    "https://deporsm-apiwith-1035693188565.us-central1.run.app",
    "https://frontend-depor-sm-leo.vercel.app",
    "http://localhost:3000"
  },
  allowCredentials = "true"
)public class MantenimientoInstalacionController {

    private final MantenimientoInstalacionRepository mantenimientoRepository;
    private final InstalacionRepository instalacionRepository;
    private final MantenimientoService mantenimientoService;

    public MantenimientoInstalacionController(
            MantenimientoInstalacionRepository mantenimientoRepository,
            InstalacionRepository instalacionRepository,
            MantenimientoService mantenimientoService) {
        this.mantenimientoRepository = mantenimientoRepository;
        this.instalacionRepository = instalacionRepository;
        this.mantenimientoService = mantenimientoService;
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
    public ResponseEntity<List<MantenimientoDTO>> obtenerActivos(
            @RequestParam(value = "fechaActual", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaParam) {

        // Si no se proporciona fecha, usar la fecha actual
        final LocalDateTime fechaActual = fechaParam != null ? fechaParam : LocalDateTime.now();

        // Obtener todos los mantenimientos y actualizar sus estados
        List<MantenimientoInstalacion> todosMantenimientos = mantenimientoRepository.findAll();
        todosMantenimientos.forEach(mantenimientoService::actualizarEstadoMantenimiento);

        // Ahora obtener los activos con los estados actualizados
        List<MantenimientoInstalacion> mantenimientos = mantenimientoRepository.findActivos(fechaActual);

        List<MantenimientoDTO> dtos = mantenimientos.stream()
            .map(m -> new MantenimientoDTO(
                m.getId(),
                m.getMotivo(),
                m.getTipo(),
                m.getDescripcion(),
                m.getFechaInicio(),
                m.getFechaFin(),
                m.getEstado(),
                m.getInstalacion().getNombre(),
                m.getInstalacion().getUbicacion()
            ))
            .toList();

        return ResponseEntity.ok(dtos);
    }

    // Mantenimientos programados: fecha futura
    @GetMapping("/programados")
    public ResponseEntity<List<MantenimientoDTO>> obtenerProgramados(
            @RequestParam(value = "fechaActual", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaParam) {

        // Si no se proporciona fecha, usar la fecha actual
        final LocalDateTime fechaActual = fechaParam != null ? fechaParam : LocalDateTime.now();

        // Obtener todos los mantenimientos y actualizar sus estados
        List<MantenimientoInstalacion> todosMantenimientos = mantenimientoRepository.findAll();
        todosMantenimientos.forEach(mantenimientoService::actualizarEstadoMantenimiento);

        // Ahora obtener los mantenimientos programados con los estados actualizados
        List<MantenimientoInstalacion> mantenimientos = mantenimientoRepository.findProgramados(fechaActual);

        List<MantenimientoDTO> dtos = mantenimientos.stream()
            .map(m -> new MantenimientoDTO(
                m.getId(),
                m.getMotivo(),
                m.getTipo(),
                m.getDescripcion(),
                m.getFechaInicio(),
                m.getFechaFin(),
                m.getEstado(),
                m.getInstalacion().getNombre(),
                m.getInstalacion().getUbicacion()
            ))
            .toList();

        return ResponseEntity.ok(dtos);
    }

    // Mantenimientos históricos: ya finalizados
    @GetMapping("/historial")
    public ResponseEntity<List<MantenimientoDTO>> obtenerHistorial(
            @RequestParam(value = "fechaActual", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaParam) {

        // Si no se proporciona fecha, usar la fecha actual
        final LocalDateTime fechaActual = fechaParam != null ? fechaParam : LocalDateTime.now();

        // Obtener todos los mantenimientos y actualizar sus estados
        List<MantenimientoInstalacion> todosMantenimientos = mantenimientoRepository.findAll();
        todosMantenimientos.forEach(mantenimientoService::actualizarEstadoMantenimiento);

        // Ahora obtener los mantenimientos finalizados con los estados actualizados
        List<MantenimientoInstalacion> mantenimientos = mantenimientoRepository.findFinalizados(fechaActual);

        List<MantenimientoDTO> dtos = mantenimientos.stream()
            .map(m -> new MantenimientoDTO(
                m.getId(),
                m.getMotivo(),
                m.getTipo(),
                m.getDescripcion(),
                m.getFechaInicio(),
                m.getFechaFin(),
                m.getEstado(),
                m.getInstalacion().getNombre(),
                m.getInstalacion().getUbicacion()
            ))
            .toList();

        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    public ResponseEntity<?> crearMantenimiento(@RequestBody MantenimientoRequestDTO requestDTO) {
        try {
            // Validar datos básicos
            if (requestDTO.getInstalacionId() == null) {
                return ResponseEntity.badRequest().body("Instalación no especificada");
            }

            if (requestDTO.getRegistradoPorId() == null) {
                return ResponseEntity.badRequest().body("Usuario que registra no especificado");
            }

            // Imprimir información detallada de las fechas recibidas
            System.out.println("=== INFORMACIÓN DE FECHAS Y HORAS RECIBIDAS ===");
            System.out.println("Fecha inicio recibida: " + requestDTO.getFechaInicio());
            System.out.println("Fecha fin recibida: " + requestDTO.getFechaFin());
            System.out.println("Zona horaria del servidor: " + java.util.TimeZone.getDefault().getID());
            System.out.println("Hora actual del servidor: " + java.time.LocalDateTime.now());
            System.out.println("==============================================");

            // Usar el servicio para programar el mantenimiento y cancelar reservas afectadas
            MantenimientoInstalacion guardado = mantenimientoService.programarMantenimiento(requestDTO);
            return ResponseEntity.ok(guardado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al crear mantenimiento: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerDetallePorId(@PathVariable Integer id) {
        return mantenimientoRepository.findById(id)
                .map(mantenimiento -> {
                    // Actualizar el estado del mantenimiento según las fechas
                    MantenimientoInstalacion actualizado = mantenimientoService.actualizarEstadoMantenimiento(mantenimiento);

                    // Crear un mapa con los datos del mantenimiento y la instalación
                    // para evitar problemas con @JsonIgnore
                    java.util.Map<String, Object> response = new java.util.HashMap<>();
                    response.put("id", actualizado.getId());
                    response.put("fechaInicio", actualizado.getFechaInicio());
                    response.put("fechaFin", actualizado.getFechaFin());
                    response.put("motivo", actualizado.getMotivo());
                    response.put("tipo", actualizado.getTipo());
                    response.put("descripcion", actualizado.getDescripcion());
                    response.put("estado", actualizado.getEstado());
                    response.put("afectaDisponibilidad", actualizado.getAfectaDisponibilidad());
                    response.put("createdAt", actualizado.getCreatedAt());
                    response.put("updatedAt", actualizado.getUpdatedAt());

                    // Incluir datos del usuario que registró
                    if (actualizado.getRegistradoPor() != null) {
                        java.util.Map<String, Object> registrador = new java.util.HashMap<>();
                        registrador.put("id", actualizado.getRegistradoPor().getId());
                        registrador.put("nombre", actualizado.getRegistradoPor().getNombre());
                        response.put("registradoPor", registrador);
                    }

                    // Incluir datos de la instalación
                    if (actualizado.getInstalacion() != null) {
                        java.util.Map<String, Object> instalacion = new java.util.HashMap<>();
                        instalacion.put("id", actualizado.getInstalacion().getId());
                        instalacion.put("nombre", actualizado.getInstalacion().getNombre());
                        instalacion.put("ubicacion", actualizado.getInstalacion().getUbicacion());
                        response.put("instalacion", instalacion);
                    }

                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarMantenimiento(@PathVariable Integer id) {
        return mantenimientoRepository.findById(id).map(mantenimiento -> {
            mantenimientoRepository.delete(mantenimiento);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Obtiene información de mantenimiento para una instalación específica
     * @param instalacionId ID de la instalación
     * @return Información sobre el último mantenimiento completado y el próximo mantenimiento programado
     */
    @GetMapping("/instalacion/{instalacionId}")
    public ResponseEntity<MantenimientoInfoDTO> obtenerInfoMantenimientoPorInstalacion(@PathVariable Integer instalacionId) {
        // Actualizar estados de todos los mantenimientos
        List<MantenimientoInstalacion> todosMantenimientos = mantenimientoRepository.findAll();
        todosMantenimientos.forEach(mantenimientoService::actualizarEstadoMantenimiento);

        // Crear el DTO de respuesta
        MantenimientoInfoDTO infoDTO = new MantenimientoInfoDTO();

        // Buscar el último mantenimiento completado
        List<MantenimientoInstalacion> mantenimientosCompletados = mantenimientoRepository.findByInstalacionIdAndEstadoOrderByFechaFinDesc(
                instalacionId, "completado");

        if (!mantenimientosCompletados.isEmpty()) {
            MantenimientoInstalacion ultimoMantenimiento = mantenimientosCompletados.get(0);
            infoDTO.setUltimoMantenimiento(ultimoMantenimiento.getFechaFin());
        }

        // Buscar mantenimientos activos (programados o en progreso)
        List<MantenimientoInstalacion> mantenimientosActivos = mantenimientoRepository.findMantenimientosActivos(instalacionId);

        if (!mantenimientosActivos.isEmpty()) {
            MantenimientoInstalacion mantenimientoActivo = mantenimientosActivos.get(0);

            // Si está en progreso, indicarlo en el DTO
            if ("en-progreso".equals(mantenimientoActivo.getEstado())) {
                infoDTO.setEnMantenimiento(true);
            }

            // Si está programado, establecer como próximo mantenimiento
            if ("programado".equals(mantenimientoActivo.getEstado())) {
                infoDTO.setProximoMantenimiento(mantenimientoActivo.getFechaInicio());
            }

            // En cualquier caso, indicar que tiene mantenimiento activo
            infoDTO.setTieneMantenimientoActivo(true);
        }

        return ResponseEntity.ok(infoDTO);
    }

    /**
     * Verifica si una instalación tiene mantenimientos activos (programados o en progreso)
     * @param instalacionId ID de la instalación
     * @return true si tiene mantenimientos activos, false en caso contrario
     */
    @GetMapping("/instalacion/{instalacionId}/tiene-activo")
    public ResponseEntity<Boolean> tieneMantenimientoActivo(@PathVariable Integer instalacionId) {
        // Actualizar estados de todos los mantenimientos
        List<MantenimientoInstalacion> todosMantenimientos = mantenimientoRepository.findAll();
        todosMantenimientos.forEach(mantenimientoService::actualizarEstadoMantenimiento);

        // Buscar mantenimientos activos (programados o en progreso)
        List<MantenimientoInstalacion> mantenimientosActivos = mantenimientoRepository.findMantenimientosActivos(instalacionId);

        return ResponseEntity.ok(!mantenimientosActivos.isEmpty());
    }

    /**
     * Obtiene las instalaciones que no tienen mantenimientos activos
     * @return Lista de instalaciones disponibles para mantenimiento
     */
    @GetMapping("/instalaciones-disponibles")
    public List<Instalacion> obtenerInstalacionesDisponiblesParaMantenimiento() {
        try {
            System.out.println("Obteniendo instalaciones disponibles para mantenimiento");

            // Obtener todas las instalaciones
            List<Instalacion> todasInstalaciones = instalacionRepository.findAll();
            System.out.println("Total de instalaciones: " + todasInstalaciones.size());

            // Enfoque simplificado: usar una consulta SQL directa para obtener instalaciones sin mantenimientos activos
            // Esto evita problemas de serialización y referencias nulas
            List<Instalacion> instalacionesDisponibles = new ArrayList<>();

            // Verificar cada instalación manualmente
            for (Instalacion instalacion : todasInstalaciones) {
                // Buscar mantenimientos activos para esta instalación específica
                List<MantenimientoInstalacion> mantenimientosProgramados =
                    mantenimientoRepository.findByInstalacionIdAndEstado(instalacion.getId(), "programado");

                List<MantenimientoInstalacion> mantenimientosEnProgreso =
                    mantenimientoRepository.findByInstalacionIdAndEstado(instalacion.getId(), "en-progreso");

                // Si no tiene mantenimientos activos, agregarla a la lista de disponibles
                if (mantenimientosProgramados.isEmpty() && mantenimientosEnProgreso.isEmpty()) {
                    instalacionesDisponibles.add(instalacion);
                    System.out.println("Instalación disponible: " + instalacion.getId() + " - " + instalacion.getNombre());
                } else {
                    System.out.println("Instalación NO disponible: " + instalacion.getId() + " - " + instalacion.getNombre());
                }
            }

            System.out.println("Instalaciones disponibles encontradas: " + instalacionesDisponibles.size());

            return instalacionesDisponibles;
        } catch (Exception e) {
            System.err.println("Error al obtener instalaciones disponibles: " + e.getMessage());
            e.printStackTrace();
            // En caso de error, devolver una lista vacía
            return new ArrayList<>();
        }
    }

    /**
     * Cancela un mantenimiento programado
     * @param id ID del mantenimiento a cancelar
     * @return ResponseEntity con el resultado de la operación
     */
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarMantenimiento(@PathVariable Integer id) {
        return mantenimientoRepository.findById(id).map(mantenimiento -> {
            // Permitir cancelar mantenimientos en cualquier estado (programado o en-progreso)
            if (!"cancelado".equals(mantenimiento.getEstado()) && !"completado".equals(mantenimiento.getEstado())) {
                mantenimiento.setEstado("cancelado");
                mantenimiento.setUpdatedAt(LocalDateTime.now());
                mantenimientoRepository.save(mantenimiento);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest()
                    .body("No se pueden cancelar mantenimientos que ya están cancelados o completados");
            }
        }).orElse(ResponseEntity.notFound().build());
    }
}
