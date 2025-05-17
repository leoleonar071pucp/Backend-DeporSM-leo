package com.example.deporsm.controller;

import com.example.deporsm.dto.DisponibilidadHorarioDTO;
import com.example.deporsm.dto.InstalacionDTO;
import com.example.deporsm.dto.InstalacionDetalleDTO;
import com.example.deporsm.dto.InstalacionEstadoDTO;
import com.example.deporsm.dto.InstalacionListDTO;
import com.example.deporsm.dto.InstalacionRequestDTO;
import com.example.deporsm.model.*;
import com.example.deporsm.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/instalaciones")
@CrossOrigin(
  origins = {
    "https://deporsm-apiwith-1035693188565.us-central1.run.app",
    "https://frontend-depor-sm-leo.vercel.app",
    "http://localhost:3000"
  },
  allowCredentials = "true"
)

public class InstalacionesController {
    
    private final InstalacionRepository repository;
    private final CaracteristicaInstalacionRepository caracteristicaRepository;
    private final ComodidadInstalacionRepository comodidadRepository;
    private final ReglaInstalacionRepository reglaRepository;
    private final HorarioDisponibleRepository horarioDisponibleRepository;
    private final CoordinadorInstalacionRepository coordinadorInstalacionRepository;

    public InstalacionesController(
            InstalacionRepository repository,
            CaracteristicaInstalacionRepository caracteristicaRepository,
            ComodidadInstalacionRepository comodidadRepository,
            ReglaInstalacionRepository reglaRepository,
            HorarioDisponibleRepository horarioDisponibleRepository,
            CoordinadorInstalacionRepository coordinadorInstalacionRepository) {
        this.repository = repository;
        this.caracteristicaRepository = caracteristicaRepository;
        this.comodidadRepository = comodidadRepository;
        this.reglaRepository = reglaRepository;
        this.horarioDisponibleRepository = horarioDisponibleRepository;
        this.coordinadorInstalacionRepository = coordinadorInstalacionRepository;
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
    public List<InstalacionDTO> filtrarPorTipo(@RequestParam String tipo) {
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

    // Se eliminó el método duplicado obtenerPorId que entraba en conflicto con obtenerInstalacionPorId

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

    /**
     * Crea una nueva instalación con sus características, comodidades y reglas
     */
    @PostMapping
    public ResponseEntity<?> crearInstalacion(@RequestBody InstalacionRequestDTO request) {
        try {
            Timestamp now = new Timestamp(System.currentTimeMillis());
            
            // Crear la instalación
            Instalacion nuevaInstalacion = new Instalacion();
            nuevaInstalacion.setNombre(request.getNombre());
            nuevaInstalacion.setDescripcion(request.getDescripcion());
            nuevaInstalacion.setUbicacion(request.getUbicacion());
            nuevaInstalacion.setTipo(request.getTipo());
            nuevaInstalacion.setCapacidad(request.getCapacidad());
            nuevaInstalacion.setHorarioApertura(request.getHorarioApertura());            nuevaInstalacion.setHorarioCierre(request.getHorarioCierre());
            nuevaInstalacion.setImagenUrl(request.getImagenUrl());
            nuevaInstalacion.setPrecio(request.getPrecio());
            nuevaInstalacion.setCreatedAt(now);
            nuevaInstalacion.setUpdatedAt(now);
            
            // Validación simple
            if (nuevaInstalacion.getActivo() == null) {
                nuevaInstalacion.setActivo(true); // por defecto activa
            }
            
            // Guardar la instalación primero para obtener su ID
            Instalacion instalacionGuardada = repository.save(nuevaInstalacion);
            
            // Procesar y guardar características si existen
            if (request.getCaracteristicas() != null && !request.getCaracteristicas().isEmpty()) {
                for (String caracteristica : request.getCaracteristicas()) {
                    CaracteristicaInstalacion nuevaCaracteristica = 
                        new CaracteristicaInstalacion(instalacionGuardada, caracteristica);
                    caracteristicaRepository.save(nuevaCaracteristica);
                }
            }
            
            // Procesar y guardar comodidades si existen
            if (request.getComodidades() != null && !request.getComodidades().isEmpty()) {
                for (String comodidad : request.getComodidades()) {
                    ComodidadInstalacion nuevaComodidad = 
                        new ComodidadInstalacion(instalacionGuardada, comodidad);
                    comodidadRepository.save(nuevaComodidad);
                }
            }
            
            // Procesar y guardar reglas si existen
            if (request.getReglas() != null && !request.getReglas().isEmpty()) {
                for (String regla : request.getReglas()) {
                    ReglaInstalacion nuevaRegla = 
                        new ReglaInstalacion(instalacionGuardada, regla);
                    reglaRepository.save(nuevaRegla);
                }
            }
            
            // Procesar y guardar horarios disponibles si existen
            if (request.getHorariosDisponibles() != null && !request.getHorariosDisponibles().isEmpty()) {
                for (InstalacionRequestDTO.HorarioDisponibleDTO horarioDTO : request.getHorariosDisponibles()) {
                    HorarioDisponible.DiaSemana diaSemana;
                    try {
                        diaSemana = HorarioDisponible.DiaSemana.valueOf(horarioDTO.getDiaSemana());
                    } catch (IllegalArgumentException e) {
                        // Si hay un error en el formato del día, lo ignoramos y continuamos
                        continue;
                    }
                    
                    // Convertir strings de hora a objetos Time
                    Time horaInicio = Time.valueOf(horarioDTO.getHoraInicio());
                    Time horaFin = Time.valueOf(horarioDTO.getHoraFin());
                    
                    HorarioDisponible nuevoHorario = 
                        new HorarioDisponible(instalacionGuardada, diaSemana, horaInicio, horaFin);
                    horarioDisponibleRepository.save(nuevoHorario);
                }
            }
            
            return ResponseEntity.ok(instalacionGuardada);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Error al crear instalación: " + e.getMessage());
        }    }
    
    @GetMapping("/estado-instalaciones")
    public List<InstalacionEstadoDTO> obtenerEstadoActualDeInstalaciones() {
        return repository.getEstadoActualInstalaciones();
    }

    // Se eliminó el método eliminarInstalacion duplicado con ruta mal formada
    // ya existe un método @DeleteMapping("/{id}") arriba
    
    /**
     * Obtiene el detalle de una instalación por su ID
     */    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerInstalacionPorId(@PathVariable Integer id) {
        try {
            return repository.findById(id)
                    .map(instalacion -> {
                        InstalacionDetalleDTO detalleDTO = new InstalacionDetalleDTO();
                        detalleDTO.setId(instalacion.getId());
                        detalleDTO.setNombre(instalacion.getNombre());
                        detalleDTO.setDescripcion(instalacion.getDescripcion());
                        detalleDTO.setUbicacion(instalacion.getUbicacion());
                        detalleDTO.setTipo(instalacion.getTipo());
                        detalleDTO.setCapacidad(instalacion.getCapacidad());
                        detalleDTO.setHorarioApertura(instalacion.getHorarioApertura());
                        detalleDTO.setHorarioCierre(instalacion.getHorarioCierre());                        detalleDTO.setImagenUrl(instalacion.getImagenUrl());
                        detalleDTO.setPrecio(instalacion.getPrecio());
                        detalleDTO.setActivo(instalacion.getActivo());
                        
                        // Verificar si está en mantenimiento (implementación simplificada)
                        detalleDTO.setEstado(instalacion.getActivo() ? "disponible" : "no disponible");
                        
                        // Obtener características
                        List<CaracteristicaInstalacion> caracteristicas = caracteristicaRepository.findByInstalacionId(id);
                        if (caracteristicas != null && !caracteristicas.isEmpty()) {
                            detalleDTO.setCaracteristicas(caracteristicas.stream()
                                .map(CaracteristicaInstalacion::getDescripcion)
                                .collect(Collectors.toList()));
                        } else {
                            detalleDTO.setCaracteristicas(new ArrayList<>());
                        }
                        
                        // Obtener comodidades
                        List<ComodidadInstalacion> comodidades = comodidadRepository.findByInstalacionId(id);
                        if (comodidades != null && !comodidades.isEmpty()) {
                            detalleDTO.setComodidades(comodidades.stream()
                                .map(ComodidadInstalacion::getDescripcion)
                                .collect(Collectors.toList()));
                        } else {
                            detalleDTO.setComodidades(new ArrayList<>());
                        }
                        
                        // Obtener reglas
                        List<ReglaInstalacion> reglas = reglaRepository.findByInstalacionId(id);
                        if (reglas != null && !reglas.isEmpty()) {
                            detalleDTO.setReglas(reglas.stream()
                                .map(ReglaInstalacion::getDescripcion)
                                .collect(Collectors.toList()));
                        } else {
                            detalleDTO.setReglas(new ArrayList<>());
                        }
                        
                        return ResponseEntity.ok(detalleDTO);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error al obtener instalación: " + e.getMessage());
        }
    }
        /**
     * Obtiene los horarios disponibles de una instalación para una fecha específica
     */
    @GetMapping("/{id}/disponibilidad")
    public ResponseEntity<?> obtenerDisponibilidad(
            @PathVariable Integer id,
            @RequestParam java.sql.Date fecha) {
        try {
            DisponibilidadHorarioDTO disponibilidad = new DisponibilidadHorarioDTO();
            disponibilidad.setFecha(fecha);
              // Obtener día de la semana de la fecha seleccionada
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(fecha);
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            
            // Convertir al formato de enum DiaSemana
            final HorarioDisponible.DiaSemana diaSemana;
            switch(dayOfWeek) {
                case Calendar.MONDAY: diaSemana = HorarioDisponible.DiaSemana.LUNES; break;
                case Calendar.TUESDAY: diaSemana = HorarioDisponible.DiaSemana.MARTES; break;
                case Calendar.WEDNESDAY: diaSemana = HorarioDisponible.DiaSemana.MIERCOLES; break;
                case Calendar.THURSDAY: diaSemana = HorarioDisponible.DiaSemana.JUEVES; break;
                case Calendar.FRIDAY: diaSemana = HorarioDisponible.DiaSemana.VIERNES; break;
                case Calendar.SATURDAY: diaSemana = HorarioDisponible.DiaSemana.SABADO; break;
                case Calendar.SUNDAY: diaSemana = HorarioDisponible.DiaSemana.DOMINGO; break;
                default: diaSemana = HorarioDisponible.DiaSemana.LUNES; // Valor por defecto
            }
            
            // Si no hay datos en la base de datos, caemos en el comportamiento anterior
            List<HorarioDisponible> horariosDisponibles = horarioDisponibleRepository
                .findDisponiblesByInstalacionAndDiaSemana(id, diaSemana);
            
            final List<DisponibilidadHorarioDTO.RangoHorarioDTO> horarios = new ArrayList<>();              if (horariosDisponibles != null && !horariosDisponibles.isEmpty()) {
                // Usar los horarios de la base de datos
                for (HorarioDisponible horario : horariosDisponibles) {
                    horarios.add(new DisponibilidadHorarioDTO.RangoHorarioDTO(
                        horario.getHoraInicio(),
                        horario.getHoraFin()
                    ));
                }
            }
            // Ya sean con horarios o sin horarios, siempre devolvemos la misma estructura
            // (con lista de horarios vacía si no hay disponibilidad)}
            
            // Crear y devolver la respuesta con los horarios encontrados
            DisponibilidadHorarioDTO disponibilidadResponse = new DisponibilidadHorarioDTO();
            disponibilidadResponse.setFecha(fecha);
            disponibilidadResponse.setHorariosDisponibles(horarios);
            return ResponseEntity.ok(disponibilidadResponse);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error al obtener disponibilidad: " + e.getMessage());
        }
    }    /**
     * Obtiene una lista de todas las instalaciones activas y disponibles
     * con estado por fecha específica
     */    @GetMapping("/disponibles")
    public ResponseEntity<?> listarInstalacionesDisponibles(
            @RequestParam(required = false) java.sql.Date fecha) {
        try {
            // Por defecto usar la fecha actual si no se especifica
            if (fecha == null) {
                fecha = new java.sql.Date(System.currentTimeMillis());
            }            // Obtener todas las instalaciones activas 
            List<Instalacion> instalacionesActivas = repository.findAll().stream()
                .filter(instalacion -> instalacion.getActivo() != null && instalacion.getActivo())
                .collect(Collectors.toList());
            
            // Convertir las instalaciones a DTOs para evitar problemas de serialización
            List<InstalacionListDTO> instalacionesDTOs = new ArrayList<>();
            for (Instalacion instalacion : instalacionesActivas) {
                InstalacionListDTO dto = new InstalacionListDTO(
                    instalacion.getId(),
                    instalacion.getNombre(),
                    instalacion.getDescripcion(),
                    instalacion.getUbicacion(),
                    instalacion.getTipo(),
                    instalacion.getCapacidad(),
                    instalacion.getHorarioApertura(),                    instalacion.getHorarioCierre(),
                    instalacion.getImagenUrl(),
                    instalacion.getPrecio(),
                    instalacion.getActivo(),
                    instalacion.getCreatedAt(),
                    instalacion.getUpdatedAt()
                );
                instalacionesDTOs.add(dto);
            }
            
            return ResponseEntity.ok(instalacionesDTOs);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error al obtener instalaciones disponibles: " + e.getMessage());
        }
    }

    /**
     * Actualiza los horarios disponibles de una instalación
     */
    @PutMapping("/{id}/horarios-disponibles")
    public ResponseEntity<?> actualizarHorariosDisponibles(
            @PathVariable Integer id,
            @RequestBody List<InstalacionRequestDTO.HorarioDisponibleDTO> horarios) {
        try {
            // Verificar que la instalación existe
            return repository.findById(id)
                    .map(instalacion -> {
                        // Eliminar horarios existentes
                        List<HorarioDisponible> horariosActuales = horarioDisponibleRepository.findByInstalacionId(id);
                        if (horariosActuales != null && !horariosActuales.isEmpty()) {
                            horarioDisponibleRepository.deleteAll(horariosActuales);
                        }
                        
                        // Guardar los nuevos horarios
                        if (horarios != null && !horarios.isEmpty()) {
                            for (InstalacionRequestDTO.HorarioDisponibleDTO horarioDTO : horarios) {
                                HorarioDisponible.DiaSemana diaSemana;
                                try {
                                    diaSemana = HorarioDisponible.DiaSemana.valueOf(horarioDTO.getDiaSemana());
                                } catch (IllegalArgumentException e) {
                                    continue;
                                }
                                
                                // Convertir strings de hora a objetos Time
                                Time horaInicio = Time.valueOf(horarioDTO.getHoraInicio());
                                Time horaFin = Time.valueOf(horarioDTO.getHoraFin());
                                
                                HorarioDisponible nuevoHorario = 
                                    new HorarioDisponible(instalacion, diaSemana, horaInicio, horaFin);
                                horarioDisponibleRepository.save(nuevoHorario);
                            }
                        }
                        
                        return ResponseEntity.ok().body("Horarios disponibles actualizados correctamente");
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error al actualizar horarios disponibles: " + e.getMessage());
        }
    }

    /**
     * Obtiene todos los horarios disponibles de una instalación
     */
    @GetMapping("/{id}/horarios-disponibles")
    public ResponseEntity<?> obtenerHorariosDisponibles(@PathVariable Integer id) {
        try {
            // Verificar que la instalación existe
            return repository.findById(id)
                    .map(instalacion -> {
                        List<HorarioDisponible> horarios = horarioDisponibleRepository.findByInstalacionId(id);
                        
                        // Convertir a DTO para la respuesta
                        List<Map<String, Object>> horariosDTO = new ArrayList<>();
                        if (horarios != null) {
                            for (HorarioDisponible horario : horarios) {
                                Map<String, Object> horarioMap = new HashMap<>();
                                horarioMap.put("id", horario.getId());
                                horarioMap.put("diaSemana", horario.getDiaSemana().name());
                                horarioMap.put("horaInicio", horario.getHoraInicio().toString());
                                horarioMap.put("horaFin", horario.getHoraFin().toString());
                                horarioMap.put("disponible", horario.getDisponible());
                                horariosDTO.add(horarioMap);
                            }
                        }
                        
                        return ResponseEntity.ok(horariosDTO);
                    })
                    .orElse(ResponseEntity.notFound().build());        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error al obtener horarios disponibles: " + e.getMessage());
        }
    }

    /**
     * Obtiene las instalaciones asignadas a un coordinador específico
     */
    @GetMapping("/coordinador/{coordinadorId}")
    public ResponseEntity<?> obtenerInstalacionesPorCoordinador(@PathVariable Integer coordinadorId) {
        try {
            // Obtener las asignaciones del coordinador
            List<CoordinadorInstalacion> asignaciones = coordinadorInstalacionRepository.findByUsuarioId(coordinadorId);
            
            if (asignaciones.isEmpty()) {
                return ResponseEntity.ok(new ArrayList<>());
            }
            
            // Extraer las instalaciones de las asignaciones
            List<Instalacion> instalaciones = asignaciones.stream()
                .map(CoordinadorInstalacion::getInstalacion)
                .collect(Collectors.toList());
            
            // Convertir a DTO simplificado para la respuesta
            List<Map<String, Object>> instalacionesDTO = instalaciones.stream()
                .map(instalacion -> {
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("id", instalacion.getId());
                    dto.put("nombre", instalacion.getNombre());
                    dto.put("tipo", instalacion.getTipo());
                    dto.put("ubicacion", instalacion.getUbicacion());
                    return dto;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(instalacionesDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error al obtener instalaciones del coordinador: " + e.getMessage());
        }
    }
}
