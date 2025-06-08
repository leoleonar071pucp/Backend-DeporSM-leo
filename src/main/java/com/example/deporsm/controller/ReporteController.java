package com.example.deporsm.controller;

import com.example.deporsm.dto.ReporteDTO;
import com.example.deporsm.dto.ReporteRequestDTO;
import com.example.deporsm.service.ReporteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reportes")
@CrossOrigin(
  origins = {
    "https://deporsm-apiwith-1035693188565.us-central1.run.app",
    "https://frontend-depor-sm-leo.vercel.app",
    "http://localhost:3000"
  },
  allowCredentials = "true"
)
public class ReporteController {

    @Autowired
    private ReporteService reporteService;

    /**
     * Genera un nuevo reporte
     */
    @PostMapping("/generar")
    public ResponseEntity<?> generarReporte(@RequestBody ReporteRequestDTO requestDTO) {
        try {
            // Obtener el usuario autenticado
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();

            ReporteDTO reporte = reporteService.generarReporte(requestDTO, email);
            return ResponseEntity.ok(reporte);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al generar reporte: " + e.getMessage());
        }
    }

    /**
     * Obtiene todos los reportes
     */
    @GetMapping
    public ResponseEntity<List<ReporteDTO>> obtenerTodosLosReportes() {
        List<ReporteDTO> reportes = reporteService.obtenerTodosLosReportes();
        return ResponseEntity.ok(reportes);
    }

    /**
     * Obtiene los reportes más recientes
     */
    @GetMapping("/recientes")
    public ResponseEntity<List<ReporteDTO>> obtenerReportesRecientes() {
        List<ReporteDTO> reportes = reporteService.obtenerReportesRecientes();
        return ResponseEntity.ok(reportes);
    }

    /**
     * Busca reportes por término de búsqueda
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<ReporteDTO>> buscarReportes(@RequestParam String query) {
        List<ReporteDTO> reportes = reporteService.buscarReportes(query);
        return ResponseEntity.ok(reportes);
    }

    /**
     * Descarga un reporte
     */
    @GetMapping("/descargar/{id}")
    public ResponseEntity<?> descargarReporte(@PathVariable Integer id) {
        try {
            System.out.println("Solicitud de descarga para reporte ID: " + id);

            // Obtener el reporte
            ReporteDTO reporte;
            try {
                reporte = reporteService.obtenerReportePorId(id);
                System.out.println("Reporte encontrado: " + reporte.getNombre());
            } catch (Exception e) {
                System.err.println("Error al obtener reporte por ID: " + e.getMessage());
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Reporte no encontrado: " + e.getMessage()));
            }

            // Obtener la URL del archivo desde Supabase
            String urlArchivo;
            try {
                urlArchivo = reporteService.obtenerUrlArchivoReporte(id);
                System.out.println("URL del archivo obtenida: " + urlArchivo);
            } catch (Exception e) {
                System.err.println("Error al obtener URL del archivo: " + e.getMessage());
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al obtener URL del archivo: " + e.getMessage()));
            }

            // Verificar que la URL existe
            if (urlArchivo == null || urlArchivo.isEmpty()) {
                System.err.println("La URL del archivo es nula o vacía");
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "URL del archivo no encontrada"));
            }

            // Retornar la URL para redirección en el frontend
            Map<String, Object> response = new HashMap<>();
            response.put("url", urlArchivo);
            response.put("nombre", reporte.getNombre());
            response.put("formato", reporte.getFormato());

            System.out.println("Enviando URL de descarga: " + urlArchivo);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error general en descargarReporte: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al descargar el reporte: " + e.getMessage()));
        }
    }

    /**
     * Obtiene un reporte por su ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerReportePorId(@PathVariable Integer id) {
        try {
            ReporteDTO reporte = reporteService.obtenerReportePorId(id);
            return ResponseEntity.ok(reporte);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Endpoint específico para generar reportes de asistencias
     */
    @PostMapping("/asistencias")
    public ResponseEntity<?> generarReporteAsistencias(@RequestBody ReporteRequestDTO requestDTO) {
        try {
            // Forzar el tipo a "asistencias"
            requestDTO.setTipo("asistencias");

            // Obtener el usuario autenticado
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();

            ReporteDTO reporte = reporteService.generarReporte(requestDTO, email);
            return ResponseEntity.ok(reporte);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al generar reporte de asistencias: " + e.getMessage());
        }
    }
}
