package com.example.deporsm.service;

import com.example.deporsm.dto.ReporteDTO;
import com.example.deporsm.dto.ReporteRequestDTO;
import com.example.deporsm.model.Instalacion;
import com.example.deporsm.model.Reporte;
import com.example.deporsm.model.Usuario;
import com.example.deporsm.repository.*;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
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

    @Value("${app.reportes.directorio:./reportes}")
    private String reportesDirectorio;

    /**
     * Genera un reporte basado en los parámetros proporcionados
     */
    public ReporteDTO generarReporte(ReporteRequestDTO requestDTO, String emailUsuario) throws Exception {
        // Verificar que el directorio de reportes exista
        crearDirectorioSiNoExiste();

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

        // Generar el nombre del archivo
        String nombreArchivo = "reporte_" + requestDTO.getTipo() + "_" + fechaInicio + "_" + fechaFin;

        if (instalacion != null) {
            nombreArchivo += "_" + instalacion.getNombre().replaceAll("\\s+", "_").toLowerCase();
        }

        nombreArchivo += "." + (requestDTO.getFormato().equals("excel") ? "csv" : "pdf");

        // Ruta completa del archivo
        String rutaArchivo = reportesDirectorio + File.separator + nombreArchivo;

        // Generar el contenido del reporte según el tipo
        String contenido = "";
        String descripcion = "";

        switch (requestDTO.getTipo()) {
            case "reservas":
                contenido = generarReporteReservas(fechaInicioDate, fechaFinDate,
                                                  instalacion, requestDTO.getFormato());
                descripcion = "Información detallada de reservas: usuarios, horarios, estados de pago";
                break;
            case "ingresos":
                contenido = generarReporteIngresos(fechaInicioDate, fechaFinDate,
                                                 instalacion, requestDTO.getFormato());
                descripcion = "Resumen de ingresos por reservas y servicios";
                break;
            case "instalaciones":
                contenido = generarReporteInstalaciones(fechaInicioDate, fechaFinDate,
                                                      instalacion, requestDTO.getFormato());
                descripcion = "Métricas de utilización: frecuencia, horarios más solicitados, capacidad";
                break;
            case "mantenimiento":
                contenido = generarReporteMantenimiento(fechaInicioDate, fechaFinDate,
                                                      instalacion, requestDTO.getFormato());
                descripcion = "Registro de mantenimientos realizados y programados";
                break;
            default:
                throw new Exception("Tipo de reporte no válido");
        }

        // Escribir el contenido al archivo
        escribirArchivo(rutaArchivo, contenido);

        // Obtener el tamaño del archivo
        String tamano = obtenerTamanoArchivo(rutaArchivo);

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
            rutaArchivo,
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
        return reporteRepository.findTop10ByOrderByFechaCreacionDesc().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
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
     * Obtiene el archivo de reporte como recurso
     */
    public Resource obtenerArchivoReporte(Integer id) throws Exception {
        Reporte reporte = reporteRepository.findById(id)
                .orElseThrow(() -> new Exception("Reporte no encontrado"));

        // Imprimir información de depuración
        System.out.println("Obteniendo archivo de reporte con ID: " + id);
        System.out.println("Ruta del archivo: " + reporte.getRutaArchivo());

        try {
            Path path = Paths.get(reporte.getRutaArchivo());

            // Verificar si el archivo existe
            if (!Files.exists(path)) {
                System.out.println("El archivo no existe en la ruta: " + path.toAbsolutePath());

                // Intentar regenerar el reporte
                System.out.println("Intentando regenerar el reporte...");

                // Obtener los datos necesarios para regenerar el reporte
                String tipo = reporte.getTipo();
                String formato = reporte.getFormato();
                LocalDate fechaInicio = LocalDate.parse(reporte.getRangoFechas().split(" - ")[0]);
                LocalDate fechaFin = LocalDate.parse(reporte.getRangoFechas().split(" - ")[1]);

                // Regenerar el contenido del reporte
                String contenido = "";
                switch (tipo) {
                    case "reservas":
                        contenido = generarReporteReservas(fechaInicio, fechaFin, reporte.getInstalacion(), formato);
                        break;
                    case "ingresos":
                        contenido = generarReporteIngresos(fechaInicio, fechaFin, reporte.getInstalacion(), formato);
                        break;
                    case "instalaciones":
                        contenido = generarReporteInstalaciones(fechaInicio, fechaFin, reporte.getInstalacion(), formato);
                        break;
                    case "mantenimiento":
                        contenido = generarReporteMantenimiento(fechaInicio, fechaFin, reporte.getInstalacion(), formato);
                        break;
                }

                // Escribir el archivo
                escribirArchivo(reporte.getRutaArchivo(), contenido);
                System.out.println("Reporte regenerado exitosamente");
            }

            Resource resource = new UrlResource(path.toUri());

            if (resource.exists() && resource.isReadable()) {
                System.out.println("Archivo encontrado y legible");
                return resource;
            } else {
                System.out.println("El archivo existe pero no es legible");
                throw new Exception("No se puede leer el archivo de reporte");
            }
        } catch (Exception e) {
            System.out.println("Error al obtener el archivo: " + e.getMessage());
            e.printStackTrace();
            throw e;
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
            contenido.append("Instalación,Total Reservas,Horas Reservadas,Porcentaje Ocupación,Horario Más Popular\n");

            // Obtener datos de uso de instalaciones y agregarlos al reporte
            List<Object[]> usos = reservaRepository.findInstalacionesUsageForReport(
                fechaInicio, fechaFin, instalacion != null ? instalacion.getId() : null);

            for (Object[] uso : usos) {
                contenido.append(String.join(",",
                    uso[0] != null ? uso[0].toString().replace(",", ";") : "",  // Instalación
                    uso[1] != null ? uso[1].toString() : "",  // Total Reservas
                    uso[2] != null ? uso[2].toString() : "",  // Horas Reservadas
                    uso[3] != null ? uso[3].toString() : "0",  // Porcentaje Ocupación
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
                contenido.append("Porcentaje Ocupación: ").append(uso[3] != null ? uso[3] + "%" : "0%").append("\n");
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
            contenido.append("ID,Instalación,Descripción,Fecha Inicio,Fecha Fin,Estado,Afecta Disponibilidad\n");

            // Obtener datos de mantenimiento y agregarlos al reporte
            List<Object[]> mantenimientos = mantenimientoInstalacionRepository.findMantenimientosForReport(
                fechaInicio, fechaFin, instalacion != null ? instalacion.getId() : null);

            for (Object[] mantenimiento : mantenimientos) {
                contenido.append(String.join(",",
                    mantenimiento[0] != null ? mantenimiento[0].toString() : "",  // ID
                    mantenimiento[1] != null ? mantenimiento[1].toString().replace(",", ";") : "",  // Instalación
                    mantenimiento[2] != null ? mantenimiento[2].toString().replace(",", ";") : "",  // Descripción
                    mantenimiento[3] != null ? mantenimiento[3].toString() : "",  // Fecha Inicio
                    mantenimiento[4] != null ? mantenimiento[4].toString() : "",  // Fecha Fin
                    mantenimiento[5] != null ? mantenimiento[5].toString() : "",  // Estado
                    mantenimiento[6] != null ? mantenimiento[6].toString() : ""   // Afecta Disponibilidad
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
                contenido.append("Descripción: ").append(mantenimiento[2]).append("\n");
                contenido.append("Fecha Inicio: ").append(mantenimiento[3]).append("\n");
                contenido.append("Fecha Fin: ").append(mantenimiento[4]).append("\n");
                contenido.append("Estado: ").append(mantenimiento[5]).append("\n");
                contenido.append("Afecta Disponibilidad: ").append(mantenimiento[6]).append("\n");
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

        // Verificar si es un archivo PDF
        if (rutaArchivo.toLowerCase().endsWith(".pdf")) {
            try {
                // Crear un documento PDF
                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(rutaArchivo));
                document.open();

                // Configurar fuente
                Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
                Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

                // Dividir el contenido en líneas
                String[] lines = contenido.split("\n");

                // Procesar cada línea
                for (String line : lines) {
                    // Detectar títulos (líneas en mayúsculas o con =)
                    if (line.equals(line.toUpperCase()) && !line.trim().isEmpty() || line.contains("=====")) {
                        // Añadir espacio antes de los títulos (excepto el primero)
                        if (!line.equals(lines[0])) {
                            document.add(new Paragraph(" "));
                        }
                        document.add(new Paragraph(line, titleFont));
                    } else if (line.trim().isEmpty()) {
                        // Línea vacía
                        document.add(new Paragraph(" "));
                    } else {
                        // Texto normal
                        document.add(new Paragraph(line, normalFont));
                    }
                }

                document.close();
                System.out.println("Archivo PDF creado correctamente: " + rutaArchivo);
            } catch (DocumentException e) {
                throw new IOException("Error al crear el documento PDF: " + e.getMessage(), e);
            }
        } else {
            // Para archivos CSV u otros formatos de texto
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(rutaArchivo))) {
                writer.write(contenido);
                writer.flush();
            }
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
            reporte.getRutaArchivo(),
            reporte.getInstalacion() != null ? reporte.getInstalacion().getId() : null
        );
    }
}

