package com.example.deporsm.controller;

import com.example.deporsm.dto.CrearReservaDTO;
import com.example.deporsm.dto.DashboardStatsDTO;
import com.example.deporsm.dto.ReservaListDTO;
import com.example.deporsm.dto.ReservaRecienteDTO;
import com.example.deporsm.model.Reserva;
import com.example.deporsm.repository.ReservaRepository;
import com.example.deporsm.service.ReservaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reservas")
@CrossOrigin(
  origins = {
    "https://deporsm-apiwith-1035693188565.us-central1.run.app",
    "https://frontend-depor-sm-leo.vercel.app",
    "http://localhost:3000"
  },
  allowCredentials = "true"
)
public class ReservaController {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ReservaService reservaService;    @GetMapping("/usuario/{dni}")
    public List<ReservaListDTO> obtenerReservasPorUsuario(@PathVariable String dni) {
        List<Reserva> reservas = reservaRepository.findByUsuario_Dni(dni);

        return reservas.stream()
                .map(reserva -> {
                    ReservaListDTO dto = new ReservaListDTO(
                            reserva.getId(),
                            reserva.getUsuario().getNombre() + " " + reserva.getUsuario().getApellidos(),
                            reserva.getInstalacion().getNombre(),
                            reserva.getInstalacion().getUbicacion(), // Incluir ubicación
                            reserva.getMetodoPago(),                // Incluir método de pago
                            reserva.getInstalacion().getImagenUrl(), // Incluir URL de imagen
                            reserva.getFecha(),
                            reserva.getHoraInicio(),
                            reserva.getHoraFin(),
                            reserva.getEstado(),
                            reserva.getEstadoPago()
                    );
                    return dto;
                })
                .collect(Collectors.toList()); // Usamos collect para compatibilidad con Java 8+
    }

    @GetMapping("/stats")
    public DashboardStatsDTO obtenerEstadisticasDashboard() {
        return reservaRepository.getDashboardStats();
    }

    /**
     * Obtiene todas las reservas para el administrador
     */
    @GetMapping("/admin")
    public ResponseEntity<?> obtenerReservasParaAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
            || authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        try {
            List<Object[]> resultados = reservaRepository.listarReservasParaAdminNative();
            List<ReservaListDTO> reservas = convertirResultadosADTO(resultados);
            return ResponseEntity.ok(reservas);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al obtener reservas: " + e.getMessage());
        }
    }

    /**
     * Filtra reservas por texto y fecha para el administrador
     */
    @GetMapping("/admin/filtrar")
    public ResponseEntity<?> filtrarReservasParaAdmin(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) String fecha,
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
            || authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        try {
            // Si se proporciona un rango de fechas, usar el nuevo método
            if ((fechaInicio != null && !fechaInicio.isEmpty()) || (fechaFin != null && !fechaFin.isEmpty())) {
                java.sql.Date fechaInicioSql = null;
                java.sql.Date fechaFinSql = null;

                if (fechaInicio != null && !fechaInicio.isEmpty()) {
                    try {
                        fechaInicioSql = java.sql.Date.valueOf(fechaInicio);
                    } catch (IllegalArgumentException e) {
                        return ResponseEntity.badRequest().body("Formato de fecha inicio inválido. Use YYYY-MM-DD");
                    }
                }

                if (fechaFin != null && !fechaFin.isEmpty()) {
                    try {
                        fechaFinSql = java.sql.Date.valueOf(fechaFin);
                    } catch (IllegalArgumentException e) {
                        return ResponseEntity.badRequest().body("Formato de fecha fin inválido. Use YYYY-MM-DD");
                    }
                }

                List<Object[]> resultados = reservaRepository.filtrarReservasPorRangoNative(texto, fechaInicioSql, fechaFinSql);
                List<ReservaListDTO> reservas = convertirResultadosADTO(resultados);
                return ResponseEntity.ok(reservas);
            } else {
                // Mantener compatibilidad con filtro de fecha única
                java.sql.Date fechaSql = null;
                if (fecha != null && !fecha.isEmpty()) {
                    try {
                        fechaSql = java.sql.Date.valueOf(fecha);
                    } catch (IllegalArgumentException e) {
                        return ResponseEntity.badRequest().body("Formato de fecha inválido. Use YYYY-MM-DD");
                    }
                }

                List<Object[]> resultados = reservaRepository.filtrarReservasNative(texto, fechaSql);
                List<ReservaListDTO> reservas = convertirResultadosADTO(resultados);
                return ResponseEntity.ok(reservas);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al filtrar reservas: " + e.getMessage());
        }
    }

    /**
     * Obtiene las reservas más recientes para mostrar en la página de inicio
     */
    @GetMapping("/recientes")
    public List<ReservaRecienteDTO> obtenerReservasRecientes() {
        return reservaRepository.obtenerReservasRecientes();
    }


    /**
     * Crea una nueva reserva para el usuario autenticado
     */
    @PostMapping
    public ResponseEntity<?> crearReserva(@RequestBody CrearReservaDTO reservaDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
            || authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        String email = authentication.getName();
        try {
            Reserva reservaCreada = reservaService.crearReserva(email, reservaDTO);
            return ResponseEntity.ok(reservaCreada);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al crear reserva: " + e.getMessage());
        }
    }

    /**
     * Cancela una reserva
     */
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarReserva(@PathVariable Integer id, @RequestParam(required = false) String motivo) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
            || authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        String email = authentication.getName();
        try {
            // Imprimir información de depuración
            System.out.println("Cancelando reserva ID: " + id + ", Usuario: " + email + ", Motivo: " + (motivo != null ? motivo : "No especificado"));

            // Llamar al servicio sin motivo si es null
            reservaService.cancelarReserva(id, email, null);
            return ResponseEntity.ok("Reserva cancelada correctamente");
        } catch (Exception e) {
            System.out.println("Error al cancelar reserva: " + e.getMessage());
            return ResponseEntity.badRequest().body("Error al cancelar reserva: " + e.getMessage());
        }
    }

    /**
     * Aprueba una reserva (cambia su estado a confirmada y su estado de pago a pagado)
     * Solo para administradores
     */
    @PutMapping("/{id}/aprobar")
    public ResponseEntity<?> aprobarReserva(@PathVariable Integer id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
            || authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        String email = authentication.getName();
        try {
            // Imprimir información de depuración
            System.out.println("Aprobando reserva ID: " + id + ", Usuario: " + email);

            reservaService.aprobarReserva(id, email);
            return ResponseEntity.ok("Reserva aprobada correctamente");
        } catch (Exception e) {
            System.out.println("Error al aprobar reserva: " + e.getMessage());
            return ResponseEntity.badRequest().body("Error al aprobar reserva: " + e.getMessage());
        }
    }
      /**
     * Obtiene el historial de reservas del usuario autenticado
     */
    @GetMapping("/historial")
    public ResponseEntity<?> historialReservas() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
            || authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }

        String email = authentication.getName();
        try {
            return ResponseEntity.ok(reservaService.obtenerHistorialReservas(email));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al obtener historial: " + e.getMessage());
        }
    }

    /**
     * Obtiene los detalles de una reserva específica por su ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerDetalleReserva(@PathVariable Integer id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
            || authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }
          try {
            return reservaRepository.obtenerDetalleReserva(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al obtener detalle de reserva: " + e.getMessage());
        }
    }

    /**
     * Obtiene las reservas recientes para una instalación específica
     */
    @GetMapping("/instalacion/{instalacionId}")
    public ResponseEntity<?> obtenerReservasPorInstalacion(@PathVariable Integer instalacionId) {
        try {
            List<ReservaRecienteDTO> reservas = reservaRepository.obtenerReservasRecientesPorInstalacion(instalacionId);

            // Imprimir información de depuración
            for (ReservaRecienteDTO reserva : reservas) {
                System.out.println("Fecha de reserva enviada al frontend: " + reserva.getFecha());
            }

            return ResponseEntity.ok(reservas);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al obtener reservas: " + e.getMessage());
        }
    }

    /**
     * Convierte los resultados de consultas nativas a DTOs
     */
    private List<ReservaListDTO> convertirResultadosADTO(List<Object[]> resultados) {
        return resultados.stream().map(row -> {
            ReservaListDTO dto = new ReservaListDTO();
            dto.setId((Integer) row[0]);
            dto.setUsuarioNombre((String) row[1]);
            dto.setInstalacionNombre((String) row[2]);
            dto.setInstalacionUbicacion((String) row[3]);
            dto.setMetodoPago((String) row[4]);
            dto.setInstalacionImagenUrl((String) row[5]);
            dto.setFecha((java.util.Date) row[6]);
            dto.setHoraInicio((java.sql.Time) row[7]);
            dto.setHoraFin((java.sql.Time) row[8]);
            dto.setEstado((String) row[9]);
            dto.setEstadoPago((String) row[10]);
            return dto;
        }).collect(Collectors.toList());
    }
}
