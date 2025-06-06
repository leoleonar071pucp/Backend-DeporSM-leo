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

            // Obtener el archivo
            Resource resource;
            try {
                resource = reporteService.obtenerArchivoReporte(id);
                System.out.println("Recurso obtenido: " + (resource != null ? "OK" : "NULL"));
            } catch (Exception e) {
                System.err.println("Error al obtener archivo de reporte: " + e.getMessage());
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al obtener archivo: " + e.getMessage()));
            }

            if (resource == null) {
                System.err.println("El recurso es NULL");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "El archivo del reporte no existe"));
            }

            // Determinar el tipo de contenido
            String contentType;
            if (reporte.getFormato().equals("excel")) {
                contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            } else {
                contentType = "application/pdf";
            }

            // Verificar que el archivo existe y es legible
            try {
                if (!resource.exists()) {
                    System.err.println("El archivo no existe: " + resource.getFilename());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "El archivo no existe",
                                    "ruta", resource.getFilename()));
                }

                if (!resource.isReadable()) {
                    System.err.println("El archivo no es legible: " + resource.getFilename());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "El archivo no es legible",
                                    "ruta", resource.getFilename()));
                }
            } catch (Exception e) {
                System.err.println("Error al verificar archivo: " + e.getMessage());
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al verificar archivo: " + e.getMessage()));
            }

            // Imprimir información de depuración
            System.out.println("Descargando reporte: " + reporte.getNombre());
            System.out.println("Ruta del archivo: " + resource.getFilename());
            System.out.println("Formato: " + reporte.getFormato());
            System.out.println("Content-Type: " + contentType);

            try {
                // Crear la respuesta
                // Crear un nombre de archivo más amigable con timestamp
                String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String filename = reporte.getNombre().replaceAll("[^a-zA-Z0-9\\s]", "_") + "_" + timestamp + "." + (reporte.getFormato().equals("excel") ? "xlsx" : "pdf");
                System.out.println("Nombre de archivo para descarga: " + filename);

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                        .body(resource);
            } catch (Exception e) {
                System.err.println("Error al crear respuesta: " + e.getMessage());
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al crear respuesta: " + e.getMessage()));
            }
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
