package com.example.deporsm.controller;

import com.example.deporsm.model.Instalacion;
import com.example.deporsm.model.Reserva;
import com.example.deporsm.model.Usuario;
import com.example.deporsm.repository.InstalacionRepository;
import com.example.deporsm.repository.UsuarioRepository;
import com.example.deporsm.service.BloqueoTemporalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@RestController
@RequestMapping("/api/chatbot")
@CrossOrigin(
  origins = {
    "https://deporsm-apiwith-1035693188565.us-central1.run.app",
    "https://frontend-depor-sm-leo.vercel.app",
    "http://localhost:3000"
  },
  allowCredentials = "true"
)
public class ChatbotController {

    @Autowired
    private InstalacionRepository instalacionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BloqueoTemporalService bloqueoTemporalService;

    // Obtener todas las instalaciones disponibles
    @GetMapping("/instalaciones")
    public ResponseEntity<List<Instalacion>> getInstalaciones() {
        List<Instalacion> instalaciones = instalacionRepository.findAll();
        return ResponseEntity.ok(instalaciones);
    }

    // Verificar disponibilidad de una instalación
    @GetMapping("/disponibilidad/{instalacionId}")
    public ResponseEntity<Map<String, Object>> verificarDisponibilidad(
            @PathVariable Long instalacionId,
            @RequestParam String fecha,
            @RequestParam String horaInicio,
            @RequestParam String horaFin) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            LocalDate fechaReserva = LocalDate.parse(fecha);
            LocalTime inicio = LocalTime.parse(horaInicio);
            LocalTime fin = LocalTime.parse(horaFin);

            // Verificar si la instalación existe
            Optional<Instalacion> instalacionOpt = instalacionRepository.findById(instalacionId.intValue());
            if (!instalacionOpt.isPresent()) {
                response.put("disponible", false);
                response.put("mensaje", "Instalación no encontrada");
                return ResponseEntity.ok(response);
            }

            Instalacion instalacion = instalacionOpt.get();

            // Convertir a tipos SQL para verificar disponibilidad
            Date fechaSQL = Date.valueOf(fechaReserva);
            Time inicioSQL = Time.valueOf(inicio);
            Time finSQL = Time.valueOf(fin);

            // Verificar disponibilidad usando el servicio de bloqueo temporal
            boolean disponible = bloqueoTemporalService.verificarDisponibilidadHorario(
                instalacion.getId(), fechaSQL, inicioSQL, finSQL);

            response.put("disponible", disponible);
            response.put("instalacion", instalacion.getNombre());
            response.put("precio", instalacion.getPrecio());
            
            if (disponible) {
                response.put("mensaje", "La instalación está disponible");
            } else {
                response.put("mensaje", "La instalación no está disponible en ese horario");
            }
            
        } catch (Exception e) {
            response.put("disponible", false);
            response.put("mensaje", "Error al verificar disponibilidad: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    // Crear una reserva desde el chatbot (simplificado para demostración)
    @PostMapping("/generar-enlace-reserva")
    public ResponseEntity<Map<String, Object>> generarEnlaceReserva(@RequestBody Map<String, Object> datos) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Extraer datos del request
            String instalacionId = datos.get("instalacionId").toString();
            String fecha = datos.get("fecha").toString();
            String horaInicio = datos.get("horaInicio").toString();
            String horaFin = datos.get("horaFin").toString();
            String usuarioDni = datos.get("usuarioDni").toString();

            // Verificar que la instalación existe
            Optional<Instalacion> instalacionOpt = instalacionRepository.findById(Integer.valueOf(instalacionId));
            if (!instalacionOpt.isPresent()) {
                response.put("exito", false);
                response.put("mensaje", "Instalación no encontrada");
                return ResponseEntity.ok(response);
            }

            // Verificar que el usuario existe
            Optional<Usuario> usuarioOpt = usuarioRepository.findByDni(usuarioDni);
            if (!usuarioOpt.isPresent()) {
                response.put("exito", false);
                response.put("mensaje", "Usuario no encontrado. Debe registrarse primero.");
                return ResponseEntity.ok(response);
            }

            // Generar link directo para facilitar la reserva
            Instalacion instalacion = instalacionOpt.get();
            Usuario usuario = usuarioOpt.get();

            // Crear URLs para ambos casos: con y sin sesión
            String linkReservaDirecto = String.format(
                "https://frontend-depor-sm-leo-leonardo-pucps-projects.vercel.app/user/reservas?instalacion=%d&fecha=%s&horaInicio=%s&horaFin=%s&dni=%s",
                instalacion.getId(), fecha, horaInicio, horaFin, usuarioDni
            );

            String linkLogin = String.format(
                "https://frontend-depor-sm-leo-leonardo-pucps-projects.vercel.app/login?redirect=/user/reservas&instalacion=%d&fecha=%s&horaInicio=%s&horaFin=%s&dni=%s",
                instalacion.getId(), fecha, horaInicio, horaFin, usuarioDni
            );

            // Generar código único para seguimiento
            String codigoSeguimiento = "CHAT_" + System.currentTimeMillis();

            response.put("exito", true);
            response.put("tipo", "enlace_reserva");
            response.put("mensaje", "¡Perfecto! He generado tus enlaces de reserva.");
            response.put("instalacion", instalacion.getNombre());
            response.put("usuario", usuario.getNombre());
            response.put("precio", instalacion.getPrecio());
            response.put("fecha", fecha);
            response.put("horaInicio", horaInicio);
            response.put("horaFin", horaFin);
            response.put("linkReservaDirecto", linkReservaDirecto);
            response.put("linkLogin", linkLogin);
            response.put("codigoSeguimiento", codigoSeguimiento);
            response.put("instrucciones", "Si ya tienes sesión iniciada, usa el primer enlace. Si no, usa el segundo que te llevará a iniciar sesión y luego a tu reserva.");

        } catch (Exception e) {
            response.put("exito", false);
            response.put("mensaje", "Error al procesar reserva: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    // Consultar horarios disponibles de forma flexible
    @GetMapping("/horarios-disponibles")
    public ResponseEntity<Map<String, Object>> consultarHorariosDisponibles(
            @RequestParam(required = false) String instalacionNombre,
            @RequestParam(required = false) Long instalacionId,
            @RequestParam(required = false) String fecha,
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Función helper para validar y parsear fechas
            LocalDate fechaInicioConsulta;
            LocalDate fechaFinConsulta;

            // Si se especifica una fecha específica, usar solo esa fecha
            if (fecha != null && !fecha.trim().isEmpty()) {
                fechaInicioConsulta = LocalDate.parse(fecha);
                fechaFinConsulta = fechaInicioConsulta;
            } else {
                // Si no se especifica fecha específica, usar rango o por defecto
                fechaInicioConsulta = (fechaInicio != null && !fechaInicio.trim().isEmpty())
                    ? LocalDate.parse(fechaInicio)
                    : LocalDate.now();
                fechaFinConsulta = (fechaFin != null && !fechaFin.trim().isEmpty())
                    ? LocalDate.parse(fechaFin)
                    : fechaInicioConsulta.plusDays(7);
            }

            List<Instalacion> instalaciones;

            // Filtrar instalaciones según los parámetros
            if (instalacionId != null) {
                Optional<Instalacion> instalacionOpt = instalacionRepository.findById(instalacionId.intValue());
                if (!instalacionOpt.isPresent()) {
                    response.put("exito", false);
                    response.put("mensaje", "Instalación no encontrada");
                    return ResponseEntity.ok(response);
                }
                instalaciones = List.of(instalacionOpt.get());
            } else if (instalacionNombre != null && !instalacionNombre.trim().isEmpty()) {
                instalaciones = instalacionRepository.findAll().stream()
                    .filter(inst -> inst.getNombre().toLowerCase().contains(instalacionNombre.toLowerCase()))
                    .toList();

                // Si no se encuentra ninguna instalación con ese nombre
                if (instalaciones.isEmpty()) {
                    response.put("exito", false);
                    response.put("mensaje", "No se encontraron instalaciones con el nombre: " + instalacionNombre);
                    return ResponseEntity.ok(response);
                }
            } else {
                instalaciones = instalacionRepository.findAll();
            }

            // Generar horarios disponibles para cada instalación
            List<Map<String, Object>> horariosDisponibles = new java.util.ArrayList<>();

            for (Instalacion instalacion : instalaciones) {
                LocalDate fechaActual = fechaInicioConsulta;

                while (!fechaActual.isAfter(fechaFinConsulta)) {
                    // Generar horarios de 6:00 AM a 10:00 PM
                    for (int hora = 6; hora < 22; hora++) {
                        LocalTime horaInicio = LocalTime.of(hora, 0);
                        LocalTime horaFin = LocalTime.of(hora + 1, 0);

                        // Verificar disponibilidad
                        Date fechaSQL = Date.valueOf(fechaActual);
                        Time inicioSQL = Time.valueOf(horaInicio);
                        Time finSQL = Time.valueOf(horaFin);

                        boolean disponible = bloqueoTemporalService.verificarDisponibilidadHorario(
                            instalacion.getId(), fechaSQL, inicioSQL, finSQL);

                        if (disponible) {
                            Map<String, Object> horario = new HashMap<>();
                            horario.put("instalacionId", instalacion.getId());
                            horario.put("instalacionNombre", instalacion.getNombre());
                            horario.put("fecha", fechaActual.toString());
                            horario.put("horaInicio", horaInicio.toString());
                            horario.put("horaFin", horaFin.toString());
                            horario.put("precio", instalacion.getPrecio());
                            horario.put("ubicacion", instalacion.getUbicacion());
                            horario.put("contacto", instalacion.getContactoNumero());
                            horariosDisponibles.add(horario);
                        }
                    }
                    fechaActual = fechaActual.plusDays(1);
                }
            }

            response.put("exito", true);
            response.put("horariosDisponibles", horariosDisponibles);
            response.put("totalHorarios", horariosDisponibles.size());
            response.put("fechaConsultaInicio", fechaInicioConsulta.toString());
            response.put("fechaConsultaFin", fechaFinConsulta.toString());
            response.put("mensaje", "Horarios disponibles encontrados");

        } catch (Exception e) {
            response.put("exito", false);
            response.put("mensaje", "Error al consultar horarios: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    // Obtener reservas de un usuario
    @GetMapping("/reservas/{dni}")
    public ResponseEntity<Map<String, Object>> getReservasUsuario(@PathVariable String dni) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Verificar que el usuario existe
            Optional<Usuario> usuarioOpt = usuarioRepository.findByDni(dni);
            if (!usuarioOpt.isPresent()) {
                response.put("exito", false);
                response.put("mensaje", "Usuario no encontrado");
                return ResponseEntity.ok(response);
            }

            // Por ahora retornamos un mensaje informativo
            // En una implementación completa, aquí se buscarían las reservas reales
            response.put("exito", true);
            response.put("mensaje", "Para consultar reservas, ingrese al sistema web");
            response.put("usuario", usuarioOpt.get().getNombre());
            response.put("total", 0);

        } catch (Exception e) {
            response.put("exito", false);
            response.put("mensaje", "Error al obtener reservas: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    // Endpoint de prueba para verificar que la API funciona
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "API Chatbot funcionando correctamente");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }
}
