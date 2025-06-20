package com.example.deporsm.service;

import com.example.deporsm.dto.ReporteDTO;
import com.example.deporsm.dto.ReporteRequestDTO;
import com.example.deporsm.model.Instalacion;
import com.example.deporsm.model.Reporte;
import com.example.deporsm.model.Usuario;
import com.example.deporsm.repository.*;
import com.example.deporsm.util.ExcelGenerator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReporteService {

    @Autowired
    private ReporteRepository reporteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private InstalacionRepository instalacionRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private MantenimientoInstalacionRepository mantenimientoInstalacionRepository;

    @Autowired
    private AsistenciaCoordinadorRepository asistenciaCoordinadorRepository;

    @Autowired
    private ExcelGenerator excelGenerator;

    @Autowired
    private PdfReportService pdfReportService;

    @Autowired
    private SupabaseStorageService supabaseStorageService;

    @Value("${app.reportes.directorio:./reportes}")
    private String reportesDirectorio;

    /**
     * Genera un reporte basado en los parámetros proporcionados
     */
    public ReporteDTO generarReporte(ReporteRequestDTO requestDTO, String emailUsuario) throws Exception {
        // Obtener el usuario que solicita el reporte
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new Exception("Usuario no encontrado"));

        // Obtener la instalación si se especificó
        Instalacion instalacion = null;
        if (requestDTO.getInstalacionId() != null) {
            instalacion = instalacionRepository.findById(requestDTO.getInstalacionId())
                    .orElseThrow(() -> new Exception("Instalación no encontrada"));
        }

        // Obtener las fechas como strings
        String fechaInicio = requestDTO.getFechaInicio();
        String fechaFin = requestDTO.getFechaFin();

        // Validar que las fechas no sean nulas
        if (fechaInicio == null || fechaFin == null) {
            throw new Exception("Las fechas de inicio y fin son obligatorias");
        }

        // Convertir strings a LocalDate para los métodos de generación de reportes
        LocalDate fechaInicioDate;
        LocalDate fechaFinDate;
        try {
            fechaInicioDate = LocalDate.parse(fechaInicio);
            fechaFinDate = LocalDate.parse(fechaFin);
        } catch (Exception e) {
            throw new Exception("Formato de fecha inválido. Use el formato yyyy-MM-dd");
        }

        // Generar nombre único para el archivo usando el servicio de Supabase
        String nombreArchivo = supabaseStorageService.generateUniqueFileName(
            requestDTO.getTipo(),
            requestDTO.getFormato(),
            instalacion != null ? instalacion.getNombre() : null
        );

        // Generar el contenido del reporte según el tipo
        String descripcion = "";

        switch (requestDTO.getTipo()) {
            case "reservas":
                descripcion = "Información detallada de reservas: usuarios, horarios, estados de pago";
                break;
            case "ingresos":
                descripcion = "Resumen de ingresos por reservas y servicios";
                break;
            case "instalaciones":
                descripcion = "Métricas de utilización: frecuencia, horarios más solicitados, capacidad";
                break;
            case "mantenimiento":
                descripcion = "Registro de mantenimientos realizados y programados";
                break;
            case "asistencias":
                descripcion = "Historial de asistencias de coordinadores";
                break;
            default:
                throw new Exception("Tipo de reporte no válido");
        }

        // Generar el archivo en memoria y subirlo a Supabase
        byte[] archivoBytes;
        String contentType;
        String tamano;
        String urlArchivo;

        if (requestDTO.getFormato().equals("excel")) {
            // Generar archivo Excel en memoria
            archivoBytes = generarArchivoExcelEnMemoria(requestDTO, fechaInicioDate, fechaFinDate, instalacion);
            contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        } else if (requestDTO.getFormato().equals("pdf")) {
            // Generar archivo PDF en memoria
            String rangoFechas = fechaInicio + " - " + fechaFin;
            archivoBytes = generarArchivoPdfEnMemoria(requestDTO, fechaInicioDate, fechaFinDate, instalacion, rangoFechas);
            contentType = "application/pdf";
        } else {
            throw new Exception("Formato de archivo no soportado: " + requestDTO.getFormato());
        }

        // Calcular el tamaño del archivo
        tamano = formatearTamanoArchivo(archivoBytes.length);

        // Subir el archivo a Supabase
        urlArchivo = supabaseStorageService.uploadReportFile(archivoBytes, nombreArchivo, contentType);

        if (urlArchivo == null) {
            throw new Exception("Error al subir el archivo a Supabase");
        }

        // Crear el rango de fechas formateado
        String rangoFechas = fechaInicio + " - " + fechaFin;

        // Crear el nombre legible del reporte
        String nombreReporte = "Reporte de " + obtenerNombreTipoReporte(requestDTO.getTipo());
        if (instalacion != null) {
            nombreReporte += " - " + instalacion.getNombre();
        }

        // Guardar los metadatos del reporte en la base de datos
        Reporte reporte = new Reporte(
            nombreReporte,
            requestDTO.getTipo(),
            requestDTO.getFormato(),
            rangoFechas,
            LocalDateTime.now(),
            usuario,
            tamano,
            descripcion,
            urlArchivo,
            instalacion
        );

        reporte = reporteRepository.save(reporte);

        // Convertir a DTO y devolver
        return convertirADTO(reporte);
    }
    /**
     * Obtiene un reporte por su ID
     */
    public ReporteDTO obtenerReportePorId(Integer id) throws Exception {
        Reporte reporte = reporteRepository.findById(id)
                .orElseThrow(() -> new Exception("Reporte no encontrado"));
        return convertirADTO(reporte);
    }

    /**
     * Obtiene todos los reportes
     */
    public List<ReporteDTO> obtenerTodosLosReportes() {
        return reporteRepository.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene los reportes más recientes
     */
    public List<ReporteDTO> obtenerReportesRecientes() {
        try {
            List<Reporte> reportes = reporteRepository.findTop10ByOrderByFechaCreacionDesc();
            if (reportes == null || reportes.isEmpty()) {
                // Si no hay reportes, devolver una lista vacía en lugar de fallar
                System.out.println("No se encontraron reportes recientes en la base de datos");
                return new ArrayList<>();
            }
            return reportes.stream()
                    .map(this::convertirADTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error al obtener reportes recientes: " + e.getMessage());
            e.printStackTrace();
            // Devolver lista vacía en caso de error
            return new ArrayList<>();
        }
    }

    /**
     * Busca reportes por término de búsqueda
     */
    public List<ReporteDTO> buscarReportes(String searchTerm) {
        return reporteRepository.searchByNombreOrTipo(searchTerm).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene la URL del archivo de reporte desde Supabase
     */
    public String obtenerUrlArchivoReporte(Integer id) throws Exception {
        Reporte reporte = reporteRepository.findById(id)
                .orElseThrow(() -> new Exception("Reporte no encontrado"));

        // Imprimir información de depuración
        System.out.println("Obteniendo URL de archivo de reporte con ID: " + id);
        System.out.println("URL del archivo: " + reporte.getUrlArchivo());

        // Retornar la URL directamente desde Supabase
        return reporte.getUrlArchivo();
    }

    /**
     * Genera un archivo PDF con diseño corporativo
     */
    private void generarArchivoPdfCorporativo(ReporteRequestDTO requestDTO, LocalDate fechaInicio, LocalDate fechaFin,
                                            Instalacion instalacion, String rutaArchivo, String rangoFechas) throws Exception {
        List<String> headers = new ArrayList<>();
        List<List<Object>> data = new ArrayList<>();

        // Obtener datos según el tipo de reporte
        switch (requestDTO.getTipo()) {
            case "reservas":
                headers = Arrays.asList("ID", "Usuario", "Instalación", "Fecha", "Hora Inicio",
                                      "Hora Fin", "Estado", "Estado Pago", "Método Pago");
                List<Object[]> reservas = reservaRepository.findReservasForReport(
                    fechaInicio, fechaFin, instalacion != null ? instalacion.getId() : null);
                for (Object[] reserva : reservas) {
                    List<Object> row = new ArrayList<>();
                    for (Object item : reserva) {
                        row.add(item != null ? item : "");
                    }
                    data.add(row);
                }
                break;

            case "mantenimiento":
                headers = Arrays.asList("Instalación", "Tipo", "Descripción", "Fecha Inicio",
                                      "Fecha Fin", "Estado", "Afecta Disponibilidad");
                List<Object[]> mantenimientos = mantenimientoInstalacionRepository.findMantenimientosForReport(
                    fechaInicio, fechaFin, instalacion != null ? instalacion.getId() : null);
                for (Object[] mantenimiento : mantenimientos) {
                    List<Object> row = new ArrayList<>();
                    // Skip the first element (ID) and add the rest
                    for (int i = 1; i < mantenimiento.length; i++) {
                        row.add(mantenimiento[i] != null ? mantenimiento[i] : "");
                    }
                    data.add(row);
                }
                break;

            case "asistencias":
                headers = Arrays.asList("Coordinador", "Instalación", "Fecha", "Hora Programada Inicio",
                                      "Hora Programada Fin", "Hora Entrada", "Estado Entrada", "Hora Salida",
                                      "Estado Salida", "Ubicación");
                List<Object[]> asistencias = asistenciaCoordinadorRepository.findAsistenciasForReport(
                    fechaInicio, fechaFin,
                    requestDTO.getCoordinadorNombre(),
                    requestDTO.getInstalacionNombre(),
                    requestDTO.getEstadoEntrada(),
                    requestDTO.getEstadoSalida());
                for (Object[] asistencia : asistencias) {
                    List<Object> row = new ArrayList<>();
                    // Skip the first element (ID) and add the rest
                    for (int i = 1; i < asistencia.length; i++) {
                        row.add(asistencia[i] != null ? asistencia[i] : "");
                    }
                    data.add(row);
                }
                break;

            default:
                throw new Exception("Tipo de reporte no soportado para PDF: " + requestDTO.getTipo());
        }

        // Generar PDF usando el servicio especializado
        pdfReportService.generarPdfCorporativo(rutaArchivo, requestDTO.getTipo(),
                                             headers, data, rangoFechas);
    }

    /**
     * Genera un archivo Excel usando Apache POI
     */
    private void generarArchivoExcel(ReporteRequestDTO requestDTO, LocalDate fechaInicio, LocalDate fechaFin,
                                   Instalacion instalacion, String rutaArchivo) throws Exception {
        try {
            System.out.println("Iniciando generación de archivo Excel: " + rutaArchivo);
            System.out.println("Tipo: " + requestDTO.getTipo() + ", Fechas: " + fechaInicio + " - " + fechaFin);

            List<String> headers = new ArrayList<>();
            List<List<Object>> data = new ArrayList<>();
            String sheetName = "";

        switch (requestDTO.getTipo()) {
            case "reservas":
                System.out.println("Generando reporte de reservas...");
                System.out.println("Fechas: " + fechaInicio + " - " + fechaFin);
                System.out.println("Instalación ID: " + (instalacion != null ? instalacion.getId() : "null"));
                headers = Arrays.asList("Usuario", "Instalación", "Fecha", "Hora Inicio",
                                      "Hora Fin", "Estado", "Estado Pago", "Método Pago");
                sheetName = "Reservas";
                List<Object[]> reservas = reservaRepository.findReservasForReport(
                    fechaInicio, fechaFin, instalacion != null ? instalacion.getId() : null);
                System.out.println("Encontradas " + reservas.size() + " reservas");

                for (int i = 0; i < reservas.size(); i++) {
                    Object[] reserva = reservas.get(i);
                    System.out.println("Procesando reserva " + (i + 1) + ":");
                    List<Object> row = new ArrayList<>();
                    // Skip the first element (ID) and add the rest
                    for (int j = 1; j < reserva.length; j++) {
                        Object item = reserva[j];
                        System.out.println("  Campo " + j + ": " + (item != null ? item.toString() + " (" + item.getClass().getSimpleName() + ")" : "null"));
                        row.add(item != null ? item : "");
                    }
                    data.add(row);
                }
                break;
            case "ingresos":
                headers = Arrays.asList("Fecha", "Instalación", "Total Reservas", "Total Ingresos");
                sheetName = "Ingresos";
                List<Object[]> ingresos = reservaRepository.findIngresosForReport(
                    fechaInicio, fechaFin, instalacion != null ? instalacion.getId() : null);
                for (Object[] ingreso : ingresos) {
                    List<Object> row = new ArrayList<>();
                    for (Object item : ingreso) {
                        row.add(item);
                    }
                    data.add(row);
                }
                break;
            case "instalaciones":
                headers = Arrays.asList("Instalación", "Total Reservas", "Horas Reservadas",
                                      "Ingresos Generados", "Horario Más Popular", "Estado Más Común", "Duración Promedio (hrs)");
                sheetName = "Uso de Instalaciones";
                List<Object[]> usos = reservaRepository.findInstalacionesUsageForReport(
                    fechaInicio, fechaFin, instalacion != null ? instalacion.getId() : null);
                for (Object[] uso : usos) {
                    List<Object> row = new ArrayList<>();
                    for (Object item : uso) {
                        row.add(item);
                    }
                    data.add(row);
                }
                break;
            case "mantenimiento":
                headers = Arrays.asList("Instalación", "Descripción", "Fecha Inicio",
                                      "Fecha Fin", "Estado", "Afecta Disponibilidad");
                sheetName = "Mantenimiento";
                List<Object[]> mantenimientos = mantenimientoInstalacionRepository.findMantenimientosForReport(
                    fechaInicio, fechaFin, instalacion != null ? instalacion.getId() : null);
                for (Object[] mantenimiento : mantenimientos) {
                    List<Object> row = new ArrayList<>();
                    // Skip the first element (ID) and add the rest
                    for (int i = 1; i < mantenimiento.length; i++) {
                        row.add(mantenimiento[i]);
                    }
                    data.add(row);
                }
                break;
            case "asistencias":
                System.out.println("Generando reporte de asistencias...");
                System.out.println("Filtros aplicados: coordinador=" + requestDTO.getCoordinadorNombre() +
                                 ", instalacion=" + requestDTO.getInstalacionNombre() +
                                 ", estadoEntrada=" + requestDTO.getEstadoEntrada() +
                                 ", estadoSalida=" + requestDTO.getEstadoSalida());
                headers = Arrays.asList("Coordinador", "Instalación", "Fecha", "Hora Programada Inicio",
                                      "Hora Programada Fin", "Hora Entrada", "Estado Entrada", "Hora Salida",
                                      "Estado Salida", "Ubicación");
                sheetName = "Asistencias";
                List<Object[]> asistencias = asistenciaCoordinadorRepository.findAsistenciasForReport(
                    fechaInicio, fechaFin,
                    requestDTO.getCoordinadorNombre(),
                    requestDTO.getInstalacionNombre(),
                    requestDTO.getEstadoEntrada(),
                    requestDTO.getEstadoSalida());
                System.out.println("Encontradas " + asistencias.size() + " asistencias");
                for (Object[] asistencia : asistencias) {
                    List<Object> row = new ArrayList<>();
                    // Skip the first element (ID) and add the rest
                    for (int i = 1; i < asistencia.length; i++) {
                        row.add(asistencia[i]);
                    }
                    data.add(row);
                }
                break;
        }

        // Generar el archivo Excel
        byte[] excelBytes = excelGenerator.generateExcel(headers, data, sheetName);

            // Escribir el archivo
            try (FileOutputStream fos = new FileOutputStream(rutaArchivo)) {
                fos.write(excelBytes);
                System.out.println("Archivo Excel generado exitosamente: " + rutaArchivo);
            }
        } catch (Exception e) {
            System.err.println("Error al generar archivo Excel: " + e.getMessage());
            e.printStackTrace();
            throw new Exception("Error al generar archivo Excel: " + e.getMessage(), e);
        }
    }

    /**
     * Genera contenido de texto para PDF u otros formatos
     */
    private String generarContenidoTexto(String tipo, LocalDate fechaInicio, LocalDate fechaFin,
                                       Instalacion instalacion, String formato) {
        switch (tipo) {
            case "reservas":
                return generarReporteReservas(fechaInicio, fechaFin, instalacion, formato);
            case "ingresos":
                return generarReporteIngresos(fechaInicio, fechaFin, instalacion, formato);
            case "instalaciones":
                return generarReporteInstalaciones(fechaInicio, fechaFin, instalacion, formato);
            case "mantenimiento":
                return generarReporteMantenimiento(fechaInicio, fechaFin, instalacion, formato);
            case "asistencias":
                return generarReporteAsistencias(fechaInicio, fechaFin, instalacion, formato);
            default:
                return "";
        }
    }

    // Métodos privados para la generación de reportes

    private String generarReporteReservas(LocalDate fechaInicio, LocalDate fechaFin,
                                         Instalacion instalacion, String formato) {
        // Implementación básica para generar el contenido del reporte de reservas
        StringBuilder contenido = new StringBuilder();

        if (formato.equals("excel")) {
            // Formato CSV para Excel
            contenido.append("ID,Usuario,Instalación,Fecha,Hora Inicio,Hora Fin,Estado,Estado Pago,Método Pago\n");

            // Obtener datos de reservas y agregarlos al reporte
            List<Object[]> reservas = reservaRepository.findReservasForReport(
                fechaInicio, fechaFin, instalacion != null ? instalacion.getId() : null);

            for (Object[] reserva : reservas) {
                contenido.append(String.join(",",
                    reserva[0] != null ? reserva[0].toString() : "",  // ID
                    reserva[1] != null ? reserva[1].toString().replace(",", ";") : "",  // Usuario
                    reserva[2] != null ? reserva[2].toString().replace(",", ";") : "",  // Instalación
                    reserva[3] != null ? reserva[3].toString() : "",  // Fecha
                    reserva[4] != null ? reserva[4].toString() : "",  // Hora Inicio
                    reserva[5] != null ? reserva[5].toString() : "",  // Hora Fin
                    reserva[6] != null ? reserva[6].toString() : "",  // Estado
                    reserva[7] != null ? reserva[7].toString() : "",  // Estado Pago
                    reserva[8] != null ? reserva[8].toString().replace(",", ";") : ""   // Método Pago
                )).append("\n");
            }
        } else {
            // Formato texto para PDF
            contenido.append("REPORTE DE RESERVAS\n");
            contenido.append("=================\n\n");
            contenido.append("Período: ").append(fechaInicio).append(" - ").append(fechaFin).append("\n");
            if (instalacion != null) {
                contenido.append("Instalación: ").append(instalacion.getNombre()).append("\n");
            }
            contenido.append("\n");

            // Obtener datos de reservas y agregarlos al reporte
            List<Object[]> reservas = reservaRepository.findReservasForReport(
                fechaInicio, fechaFin, instalacion != null ? instalacion.getId() : null);

            for (Object[] reserva : reservas) {
                contenido.append("ID: ").append(reserva[0]).append("\n");
                contenido.append("Usuario: ").append(reserva[1]).append("\n");
                contenido.append("Instalación: ").append(reserva[2]).append("\n");
                contenido.append("Fecha: ").append(reserva[3]).append("\n");
                contenido.append("Horario: ").append(reserva[4]).append(" - ").append(reserva[5]).append("\n");
                contenido.append("Estado: ").append(reserva[6]).append("\n");
                contenido.append("Estado Pago: ").append(reserva[7]).append("\n");
                contenido.append("Método Pago: ").append(reserva[8]).append("\n");
                contenido.append("-------------------\n");
            }
        }

        return contenido.toString();
    }

    private String generarReporteIngresos(LocalDate fechaInicio, LocalDate fechaFin,
                                         Instalacion instalacion, String formato) {
        // Implementación para generar el contenido del reporte de ingresos
        StringBuilder contenido = new StringBuilder();

        if (formato.equals("excel")) {
            // Formato CSV para Excel
            contenido.append("Fecha,Instalación,Total Reservas,Total Ingresos\n");

            // Obtener datos de ingresos y agregarlos al reporte
            List<Object[]> ingresos = reservaRepository.findIngresosForReport(
                fechaInicio, fechaFin, instalacion != null ? instalacion.getId() : null);

            for (Object[] ingreso : ingresos) {
                contenido.append(String.join(",",
                    ingreso[0] != null ? ingreso[0].toString() : "",  // Fecha
                    ingreso[1] != null ? ingreso[1].toString().replace(",", ";") : "",  // Instalación
                    ingreso[2] != null ? ingreso[2].toString() : "",  // Total Reservas
                    ingreso[3] != null ? ingreso[3].toString() : ""   // Total Ingresos
                )).append("\n");
            }
        } else {
            // Formato texto para PDF
            contenido.append("REPORTE DE INGRESOS\n");
            contenido.append("=================\n\n");
            contenido.append("Período: ").append(fechaInicio).append(" - ").append(fechaFin).append("\n");
            if (instalacion != null) {
                contenido.append("Instalación: ").append(instalacion.getNombre()).append("\n");
            }
            contenido.append("\n");

            // Obtener datos de ingresos y agregarlos al reporte
            List<Object[]> ingresos = reservaRepository.findIngresosForReport(
                fechaInicio, fechaFin, instalacion != null ? instalacion.getId() : null);

            for (Object[] ingreso : ingresos) {
                contenido.append("Fecha: ").append(ingreso[0]).append("\n");
                contenido.append("Instalación: ").append(ingreso[1]).append("\n");
                contenido.append("Total Reservas: ").append(ingreso[2]).append("\n");
                contenido.append("Total Ingresos: S/. ").append(ingreso[3]).append("\n");
                contenido.append("-------------------\n");
            }
        }

        return contenido.toString();
    }
    private String generarReporteInstalaciones(LocalDate fechaInicio, LocalDate fechaFin,
                                            Instalacion instalacion, String formato) {
        // Implementación para generar el contenido del reporte de uso de instalaciones
        StringBuilder contenido = new StringBuilder();

        if (formato.equals("excel")) {
            // Formato CSV para Excel
            contenido.append("Instalación,Total Reservas,Horas Reservadas,Ingresos Generados,Horario Más Popular\n");

            // Obtener datos de uso de instalaciones y agregarlos al reporte
            List<Object[]> usos = reservaRepository.findInstalacionesUsageForReport(
                fechaInicio, fechaFin, instalacion != null ? instalacion.getId() : null);

            for (Object[] uso : usos) {
                contenido.append(String.join(",",
                    uso[0] != null ? uso[0].toString().replace(",", ";") : "",  // Instalación
                    uso[1] != null ? uso[1].toString() : "",  // Total Reservas
                    uso[2] != null ? uso[2].toString() : "",  // Horas Reservadas
                    uso[3] != null ? uso[3].toString() : "0",  // Ingresos Generados
                    uso[4] != null ? uso[4].toString().replace(",", ";") : ""   // Horario Más Popular
                )).append("\n");
            }
        } else {
            // Formato texto para PDF
            contenido.append("REPORTE DE USO DE INSTALACIONES\n");
            contenido.append("=============================\n\n");
            contenido.append("Período: ").append(fechaInicio).append(" - ").append(fechaFin).append("\n");
            if (instalacion != null) {
                contenido.append("Instalación: ").append(instalacion.getNombre()).append("\n");
            }
            contenido.append("\n");

            // Obtener datos de uso de instalaciones y agregarlos al reporte
            List<Object[]> usos = reservaRepository.findInstalacionesUsageForReport(
                fechaInicio, fechaFin, instalacion != null ? instalacion.getId() : null);

            for (Object[] uso : usos) {
                contenido.append("Instalación: ").append(uso[0]).append("\n");
                contenido.append("Total Reservas: ").append(uso[1]).append("\n");
                contenido.append("Horas Reservadas: ").append(uso[2]).append("\n");
                contenido.append("Ingresos Generados: S/. ").append(uso[3] != null ? uso[3] : "0").append("\n");
                contenido.append("Horario Más Popular: ").append(uso[4] != null ? uso[4] : "No disponible").append("\n");
                contenido.append("-------------------\n");
            }
        }

        return contenido.toString();
    }

    private String generarReporteMantenimiento(LocalDate fechaInicio, LocalDate fechaFin,
                                              Instalacion instalacion, String formato) {
        // Implementación para generar el contenido del reporte de mantenimiento
        StringBuilder contenido = new StringBuilder();

        if (formato.equals("excel")) {
            // Formato CSV para Excel
            contenido.append("ID,Instalación,Tipo,Descripción,Fecha Inicio,Fecha Fin,Estado,Afecta Disponibilidad\n");

            // Obtener datos de mantenimiento y agregarlos al reporte
            List<Object[]> mantenimientos = mantenimientoInstalacionRepository.findMantenimientosForReport(
                fechaInicio, fechaFin, instalacion != null ? instalacion.getId() : null);

            for (Object[] mantenimiento : mantenimientos) {
                contenido.append(String.join(",",
                    mantenimiento[0] != null ? mantenimiento[0].toString() : "",  // ID
                    mantenimiento[1] != null ? mantenimiento[1].toString().replace(",", ";") : "",  // Instalación
                    mantenimiento[2] != null ? mantenimiento[2].toString().replace(",", ";") : "",  // Tipo
                    mantenimiento[3] != null ? mantenimiento[3].toString().replace(",", ";") : "",  // Descripción
                    mantenimiento[4] != null ? mantenimiento[4].toString() : "",  // Fecha Inicio
                    mantenimiento[5] != null ? mantenimiento[5].toString() : "",  // Fecha Fin
                    mantenimiento[6] != null ? mantenimiento[6].toString() : "",  // Estado
                    mantenimiento[7] != null ? mantenimiento[7].toString() : ""   // Afecta Disponibilidad
                )).append("\n");
            }
        } else {
            // Formato texto para PDF
            contenido.append("REPORTE DE MANTENIMIENTO\n");
            contenido.append("=======================\n\n");
            contenido.append("Período: ").append(fechaInicio).append(" - ").append(fechaFin).append("\n");
            if (instalacion != null) {
                contenido.append("Instalación: ").append(instalacion.getNombre()).append("\n");
            }
            contenido.append("\n");

            // Obtener datos de mantenimiento y agregarlos al reporte
            List<Object[]> mantenimientos = mantenimientoInstalacionRepository.findMantenimientosForReport(
                fechaInicio, fechaFin, instalacion != null ? instalacion.getId() : null);

            for (Object[] mantenimiento : mantenimientos) {
                contenido.append("ID: ").append(mantenimiento[0]).append("\n");
                contenido.append("Instalación: ").append(mantenimiento[1]).append("\n");
                contenido.append("Tipo: ").append(mantenimiento[2]).append("\n");
                contenido.append("Descripción: ").append(mantenimiento[3]).append("\n");
                contenido.append("Fecha Inicio: ").append(mantenimiento[4]).append("\n");
                contenido.append("Fecha Fin: ").append(mantenimiento[5]).append("\n");
                contenido.append("Estado: ").append(mantenimiento[6]).append("\n");
                contenido.append("Afecta Disponibilidad: ").append(mantenimiento[7]).append("\n");
                contenido.append("-------------------\n");
            }
        }

        return contenido.toString();
    }
    /**
     * Crea el directorio de reportes si no existe
     */
    private void crearDirectorioSiNoExiste() throws IOException {
        File directorio = new File(reportesDirectorio);
        if (!directorio.exists()) {
            System.out.println("Creando directorio de reportes: " + directorio.getAbsolutePath());
            if (!directorio.mkdirs()) {
                throw new IOException("No se pudo crear el directorio de reportes: " + directorio.getAbsolutePath());
            }
        }
    }

    /**
     * Escribe el contenido al archivo
     */
    private void escribirArchivo(String rutaArchivo, String contenido) throws IOException {
        // Asegurarse de que el directorio padre exista
        File file = new File(rutaArchivo);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            System.out.println("Creando directorio padre: " + parentDir.getAbsolutePath());
            if (!parentDir.mkdirs()) {
                throw new IOException("No se pudo crear el directorio: " + parentDir.getAbsolutePath());
            }
        }

        System.out.println("Escribiendo archivo en: " + rutaArchivo);
        System.out.println("Tamaño del contenido: " + contenido.length() + " caracteres");

        // Para archivos CSV u otros formatos de texto (PDF ya no se maneja aquí)
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(rutaArchivo))) {
            writer.write(contenido);
            writer.flush();
        }

        // Verificar que el archivo se creó correctamente
        File createdFile = new File(rutaArchivo);
        if (!createdFile.exists()) {
            throw new IOException("El archivo no se creó correctamente: " + rutaArchivo);
        }

        System.out.println("Archivo escrito correctamente. Tamaño: " + createdFile.length() + " bytes");
    }

    private String obtenerTamanoArchivo(String rutaArchivo) {
        try {
            long bytes = Files.size(Paths.get(rutaArchivo));
            if (bytes < 1024) {
                return bytes + " B";
            } else if (bytes < 1024 * 1024) {
                return (bytes / 1024) + " KB";
            } else {
                return (bytes / (1024 * 1024)) + " MB";
            }
        } catch (IOException e) {
            return "Desconocido";
        }
    }

    private String obtenerNombreTipoReporte(String tipo) {
        switch (tipo) {
            case "reservas": return "Reservas";
            case "ingresos": return "Ingresos";
            case "instalaciones": return "Uso de Instalaciones";
            case "mantenimiento": return "Mantenimiento";
            case "asistencias": return "Asistencias";
            default: return tipo;
        }
    }

    private ReporteDTO convertirADTO(Reporte reporte) {
        return new ReporteDTO(
            reporte.getId(),
            reporte.getNombre(),
            reporte.getTipo(),
            reporte.getFormato(),
            reporte.getRangoFechas(),
            reporte.getFechaCreacion(),
            reporte.getUsuario().getNombre() + " " + reporte.getUsuario().getApellidos(),
            reporte.getTamano(),
            reporte.getDescripcion(),
            reporte.getUrlArchivo(),
            reporte.getInstalacion() != null ? reporte.getInstalacion().getId() : null
        );
    }

    /**
     * Formatea el tamaño del archivo en bytes a una representación legible
     */
    private String formatearTamanoArchivo(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        }
    }

    /**
     * Genera un archivo Excel en memoria
     */
    private byte[] generarArchivoExcelEnMemoria(ReporteRequestDTO requestDTO, LocalDate fechaInicio, LocalDate fechaFin,
                                              Instalacion instalacion) throws Exception {
        try {
            System.out.println("Iniciando generación de archivo Excel en memoria");
            System.out.println("Tipo: " + requestDTO.getTipo() + ", Fechas: " + fechaInicio + " - " + fechaFin);

            List<String> headers = new ArrayList<>();
            List<List<Object>> data = new ArrayList<>();
            String sheetName = "";

            switch (requestDTO.getTipo()) {
                case "reservas":
                    System.out.println("Generando reporte de reservas...");
                    headers = Arrays.asList("Usuario", "Instalación", "Fecha", "Hora Inicio",
                                          "Hora Fin", "Estado", "Estado Pago", "Método Pago");
                    sheetName = "Reservas";
                    List<Object[]> reservas = reservaRepository.findReservasForReport(
                        fechaInicio, fechaFin, instalacion != null ? instalacion.getId() : null);
                    System.out.println("Encontradas " + reservas.size() + " reservas");

                    for (Object[] reserva : reservas) {
                        List<Object> row = new ArrayList<>();
                        // Skip the first element (ID) and add the rest
                        for (int i = 1; i < reserva.length; i++) {
                            row.add(reserva[i] != null ? reserva[i] : "");
                        }
                        data.add(row);
                    }
                    break;
                case "ingresos":
                    headers = Arrays.asList("Fecha", "Instalación", "Total Reservas", "Total Ingresos");
                    sheetName = "Ingresos";
                    List<Object[]> ingresos = reservaRepository.findIngresosForReport(
                        fechaInicio, fechaFin, instalacion != null ? instalacion.getId() : null);
                    for (Object[] ingreso : ingresos) {
                        List<Object> row = new ArrayList<>();
                        for (Object item : ingreso) {
                            row.add(item);
                        }
                        data.add(row);
                    }
                    break;
                case "instalaciones":
                    headers = Arrays.asList("Instalación", "Total Reservas", "Horas Reservadas",
                                          "Ingresos Generados", "Horario Más Popular");
                    sheetName = "Uso de Instalaciones";
                    List<Object[]> usos = reservaRepository.findInstalacionesUsageForReport(
                        fechaInicio, fechaFin, instalacion != null ? instalacion.getId() : null);
                    for (Object[] uso : usos) {
                        List<Object> row = new ArrayList<>();
                        for (Object item : uso) {
                            row.add(item);
                        }
                        data.add(row);
                    }
                    break;
                case "mantenimiento":
                    headers = Arrays.asList("Instalación", "Tipo", "Descripción", "Fecha Inicio",
                                          "Fecha Fin", "Estado", "Afecta Disponibilidad");
                    sheetName = "Mantenimiento";
                    List<Object[]> mantenimientos = mantenimientoInstalacionRepository.findMantenimientosForReport(
                        fechaInicio, fechaFin, instalacion != null ? instalacion.getId() : null);
                    for (Object[] mantenimiento : mantenimientos) {
                        List<Object> row = new ArrayList<>();
                        // Skip the first element (ID) and add the rest
                        for (int i = 1; i < mantenimiento.length; i++) {
                            row.add(mantenimiento[i]);
                        }
                        data.add(row);
                    }
                    break;
                case "asistencias":
                    System.out.println("Generando reporte de asistencias...");
                    headers = Arrays.asList("Coordinador", "Instalación", "Fecha", "Hora Programada Inicio",
                                          "Hora Programada Fin", "Hora Entrada", "Estado Entrada", "Hora Salida",
                                          "Estado Salida", "Ubicación");
                    sheetName = "Asistencias";
                    List<Object[]> asistencias = asistenciaCoordinadorRepository.findAsistenciasForReport(
                        fechaInicio, fechaFin,
                        requestDTO.getCoordinadorNombre(),
                        requestDTO.getInstalacionNombre(),
                        requestDTO.getEstadoEntrada(),
                        requestDTO.getEstadoSalida());
                    System.out.println("Encontradas " + asistencias.size() + " asistencias");
                    for (Object[] asistencia : asistencias) {
                        List<Object> row = new ArrayList<>();
                        // Skip the first element (ID) and add the rest
                        for (int i = 1; i < asistencia.length; i++) {
                            row.add(asistencia[i]);
                        }
                        data.add(row);
                    }
                    break;
            }

            // Generar el archivo Excel en memoria
            byte[] excelBytes = excelGenerator.generateExcel(headers, data, sheetName);
            System.out.println("Archivo Excel generado exitosamente en memoria. Tamaño: " + excelBytes.length + " bytes");
            return excelBytes;

        } catch (Exception e) {
            System.err.println("Error al generar archivo Excel en memoria: " + e.getMessage());
            e.printStackTrace();
            throw new Exception("Error al generar archivo Excel en memoria: " + e.getMessage(), e);
        }
    }

    /**
     * Genera un archivo PDF en memoria
     */
    private byte[] generarArchivoPdfEnMemoria(ReporteRequestDTO requestDTO, LocalDate fechaInicio, LocalDate fechaFin,
                                            Instalacion instalacion, String rangoFechas) throws Exception {
        List<String> headers = new ArrayList<>();
        List<List<Object>> data = new ArrayList<>();

        // Obtener datos según el tipo de reporte
        switch (requestDTO.getTipo()) {
            case "reservas":
                headers = Arrays.asList("Usuario", "Instalación", "Fecha", "Hora Inicio",
                                      "Hora Fin", "Estado", "Estado Pago", "Método Pago");
                List<Object[]> reservas = reservaRepository.findReservasForReport(
                    fechaInicio, fechaFin, instalacion != null ? instalacion.getId() : null);
                for (Object[] reserva : reservas) {
                    List<Object> row = new ArrayList<>();
                    // Skip the first element (ID) and add the rest
                    for (int i = 1; i < reserva.length; i++) {
                        row.add(reserva[i] != null ? reserva[i] : "");
                    }
                    data.add(row);
                }
                break;

            case "mantenimiento":
                headers = Arrays.asList("Instalación", "Tipo", "Descripción", "Fecha Inicio",
                                      "Fecha Fin", "Estado", "Afecta Disponibilidad");
                List<Object[]> mantenimientos = mantenimientoInstalacionRepository.findMantenimientosForReport(
                    fechaInicio, fechaFin, instalacion != null ? instalacion.getId() : null);
                for (Object[] mantenimiento : mantenimientos) {
                    List<Object> row = new ArrayList<>();
                    // Skip the first element (ID) and add the rest
                    for (int i = 1; i < mantenimiento.length; i++) {
                        row.add(mantenimiento[i] != null ? mantenimiento[i] : "");
                    }
                    data.add(row);
                }
                break;

            case "ingresos":
                headers = Arrays.asList("Fecha", "Instalación", "Total Reservas", "Total Ingresos");
                List<Object[]> ingresos = reservaRepository.findIngresosForReport(
                    fechaInicio, fechaFin, instalacion != null ? instalacion.getId() : null);
                for (Object[] ingreso : ingresos) {
                    List<Object> row = new ArrayList<>();
                    for (Object item : ingreso) {
                        row.add(item != null ? item : "");
                    }
                    data.add(row);
                }
                break;

            case "instalaciones":
                headers = Arrays.asList("Instalación", "Total Reservas", "Horas Reservadas",
                                      "Ingresos Generados", "Horario Más Popular");
                List<Object[]> usos = reservaRepository.findInstalacionesUsageForReport(
                    fechaInicio, fechaFin, instalacion != null ? instalacion.getId() : null);
                for (Object[] uso : usos) {
                    List<Object> row = new ArrayList<>();
                    for (Object item : uso) {
                        row.add(item != null ? item : "");
                    }
                    data.add(row);
                }
                break;

            case "asistencias":
                headers = Arrays.asList("Coordinador", "Instalación", "Fecha", "Hora Programada Inicio",
                                      "Hora Programada Fin", "Hora Entrada", "Estado Entrada", "Hora Salida",
                                      "Estado Salida", "Ubicación");
                List<Object[]> asistencias = asistenciaCoordinadorRepository.findAsistenciasForReport(
                    fechaInicio, fechaFin,
                    requestDTO.getCoordinadorNombre(),
                    requestDTO.getInstalacionNombre(),
                    requestDTO.getEstadoEntrada(),
                    requestDTO.getEstadoSalida());
                for (Object[] asistencia : asistencias) {
                    List<Object> row = new ArrayList<>();
                    // Skip the first element (ID) and add the rest
                    for (int i = 1; i < asistencia.length; i++) {
                        row.add(asistencia[i] != null ? asistencia[i] : "");
                    }
                    data.add(row);
                }
                break;

            default:
                throw new Exception("Tipo de reporte no soportado para PDF: " + requestDTO.getTipo());
        }

        // Generar PDF usando el servicio especializado y retornar bytes
        return pdfReportService.generarPdfCorporativoEnMemoria(requestDTO.getTipo(), headers, data, rangoFechas);
    }

    /**
     * Genera reporte de asistencias de coordinadores
     */
    private String generarReporteAsistencias(LocalDate fechaInicio, LocalDate fechaFin, Instalacion instalacion, String formato) {
        List<Object[]> asistencias = asistenciaCoordinadorRepository.findAsistenciasForReport(
            fechaInicio, fechaFin, null, null, null, null);

        StringBuilder contenido = new StringBuilder();

        if (formato.equals("pdf")) {
            contenido.append("REPORTE DE ASISTENCIAS DE COORDINADORES\n");
            contenido.append("==========================================\n\n");
            contenido.append("Período: ").append(fechaInicio).append(" - ").append(fechaFin).append("\n\n");

            for (Object[] asistencia : asistencias) {
                contenido.append("ID: ").append(asistencia[0]).append("\n");
                contenido.append("Coordinador: ").append(asistencia[1]).append("\n");
                contenido.append("Instalación: ").append(asistencia[2]).append("\n");
                contenido.append("Fecha: ").append(asistencia[3]).append("\n");
                contenido.append("Horario Programado: ").append(asistencia[4]).append(" - ").append(asistencia[5]).append("\n");
                contenido.append("Hora Entrada: ").append(asistencia[6] != null ? asistencia[6] : "No registrada").append("\n");
                contenido.append("Estado Entrada: ").append(asistencia[7] != null ? asistencia[7] : "Pendiente").append("\n");
                contenido.append("Hora Salida: ").append(asistencia[8] != null ? asistencia[8] : "No registrada").append("\n");
                contenido.append("Estado Salida: ").append(asistencia[9] != null ? asistencia[9] : "Pendiente").append("\n");
                contenido.append("Ubicación: ").append(asistencia[10] != null ? asistencia[10] : "No registrada").append("\n");
                contenido.append("------------------\n\n");
            }
        } else {
            // Formato CSV (aunque ya no se usa, mantenemos compatibilidad)
            contenido.append("ID,Coordinador,Instalación,Fecha,Hora Programada Inicio,Hora Programada Fin,Hora Entrada,Estado Entrada,Hora Salida,Estado Salida,Ubicación\n");

            for (Object[] asistencia : asistencias) {
                for (int i = 0; i < asistencia.length; i++) {
                    if (i > 0) contenido.append(",");
                    Object valor = asistencia[i];
                    if (valor != null) {
                        String valorStr = valor.toString().replace(",", ";");
                        contenido.append("\"").append(valorStr).append("\"");
                    } else {
                        contenido.append("\"\"");
                    }
                }
                contenido.append("\n");
            }
        }

        return contenido.toString();
    }

    /**
     * Genera una ruta de archivo única para evitar conflictos con archivos en uso
     */
    private String generarRutaArchivoUnica(String rutaOriginal) {
        File archivo = new File(rutaOriginal);

        // Si el archivo no existe, usar la ruta original
        if (!archivo.exists()) {
            return rutaOriginal;
        }

        // Si el archivo existe, verificar si está en uso
        if (!isFileInUse(archivo)) {
            // Si no está en uso, intentar eliminarlo para usar el mismo nombre
            try {
                if (archivo.delete()) {
                    System.out.println("Archivo anterior eliminado: " + rutaOriginal);
                    return rutaOriginal;
                }
            } catch (Exception e) {
                System.out.println("No se pudo eliminar el archivo anterior: " + e.getMessage());
            }
        }

        // Si está en uso o no se pudo eliminar, generar un nombre único
        String directorio = archivo.getParent();
        String nombreSinExtension = archivo.getName().substring(0, archivo.getName().lastIndexOf('.'));
        String extension = archivo.getName().substring(archivo.getName().lastIndexOf('.'));

        int contador = 1;
        String nuevaRuta;
        do {
            String nuevoNombre = nombreSinExtension + "_(" + contador + ")" + extension;
            nuevaRuta = directorio + File.separator + nuevoNombre;
            contador++;
        } while (new File(nuevaRuta).exists() && isFileInUse(new File(nuevaRuta)));

        System.out.println("Generando archivo con nombre único: " + nuevaRuta);
        return nuevaRuta;
    }

    /**
     * Verifica si un archivo está siendo usado por otro proceso
     */
    private boolean isFileInUse(File archivo) {
        if (!archivo.exists()) {
            return false;
        }

        try {
            // Intentar renombrar el archivo temporalmente
            File tempFile = new File(archivo.getAbsolutePath() + ".tmp");
            boolean renamed = archivo.renameTo(tempFile);
            if (renamed) {
                // Si se pudo renombrar, restaurar el nombre original
                tempFile.renameTo(archivo);
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Limpia archivos de reportes antiguos (más de 7 días)
     */
    private void limpiarArchivosAntiguos() {
        try {
            File directorio = new File(reportesDirectorio);
            if (!directorio.exists()) {
                return;
            }

            long tiempoLimite = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L); // 7 días
            File[] archivos = directorio.listFiles();

            if (archivos != null) {
                int archivosEliminados = 0;
                for (File archivo : archivos) {
                    if (archivo.isFile() && archivo.lastModified() < tiempoLimite) {
                        if (!isFileInUse(archivo)) {
                            if (archivo.delete()) {
                                archivosEliminados++;
                                System.out.println("Archivo antiguo eliminado: " + archivo.getName());
                            }
                        }
                    }
                }
                if (archivosEliminados > 0) {
                    System.out.println("Se eliminaron " + archivosEliminados + " archivos antiguos");
                }
            }
        } catch (Exception e) {
            System.err.println("Error al limpiar archivos antiguos: " + e.getMessage());
        }
    }
}

