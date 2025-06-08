package com.example.deporsm.service;

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfReportService {

    // Colores corporativos - celeste como la página web
    private static final Color CELESTE_HEADER = new DeviceRgb(12, 183, 242); // #0cb7f2 - color principal de la página
    private static final Color YELLOW_SEPARATOR = new DeviceRgb(241, 196, 15);
    private static final Color GRAY_TEXT = new DeviceRgb(52, 73, 94);
    private static final Color WHITE = new DeviceRgb(255, 255, 255);

    /**
     * Genera un PDF con diseño corporativo profesional
     */
    public void generarPdfCorporativo(String rutaArchivo, String tipoReporte, List<String> headers,
                                    List<List<Object>> data, String rangoFechas) throws IOException {
        System.out.println("=== GENERANDO PDF CORPORATIVO ===");
        System.out.println("Tipo: " + tipoReporte);
        System.out.println("Headers: " + headers);
        System.out.println("Cantidad de datos: " + data.size());
        System.out.println("Rango fechas: " + rangoFechas);

        try {
            PdfWriter writer = new PdfWriter(new FileOutputStream(rutaArchivo));
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, PageSize.A4);

            // Configurar márgenes para el contenido (evitar header y footer)
            document.setMargins(100, 50, 80, 50); // top aumentado para header más alto

            // Agregar manejador de eventos para header, footer y marca de agua
            pdfDoc.addEventHandler(PdfDocumentEvent.END_PAGE, new HeaderFooterHandler(tipoReporte));

            // Solo agregar línea separadora inicial
            agregarLineaSeparadora(document);

            // Agregar contenido según el tipo de reporte
            agregarContenidoReporte(document, data, headers);

            document.close();
            System.out.println("PDF generado exitosamente: " + rutaArchivo);
        } catch (Exception e) {
            System.err.println("Error al generar PDF: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Error al generar PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Genera un PDF con diseño corporativo profesional en memoria
     */
    public byte[] generarPdfCorporativoEnMemoria(String tipoReporte, List<String> headers,
                                               List<List<Object>> data, String rangoFechas) throws IOException {
        System.out.println("=== GENERANDO PDF CORPORATIVO EN MEMORIA ===");
        System.out.println("Tipo: " + tipoReporte);
        System.out.println("Headers: " + headers);
        System.out.println("Cantidad de datos: " + data.size());
        System.out.println("Rango fechas: " + rangoFechas);

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, PageSize.A4);

            // Configurar márgenes para el contenido (evitar header y footer)
            document.setMargins(100, 50, 80, 50); // top aumentado para header más alto

            // Agregar manejador de eventos para header, footer y marca de agua
            pdfDoc.addEventHandler(PdfDocumentEvent.END_PAGE, new HeaderFooterHandler(tipoReporte));

            // Solo agregar línea separadora inicial
            agregarLineaSeparadora(document);

            // Agregar contenido según el tipo de reporte
            agregarContenidoReporte(document, data, headers);

            document.close();

            byte[] pdfBytes = baos.toByteArray();
            System.out.println("PDF generado exitosamente en memoria. Tamaño: " + pdfBytes.length + " bytes");
            return pdfBytes;

        } catch (Exception e) {
            System.err.println("Error al generar PDF en memoria: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Error al generar PDF en memoria: " + e.getMessage(), e);
        }
    }

    /**
     * Agrega el título principal del reporte
     */
    private void agregarTituloPrincipal(Document document, String tipoReporte, String rangoFechas) {
        // Título del reporte
        Paragraph titulo = new Paragraph("Reporte de " + obtenerNombreTipoReporte(tipoReporte))
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10);
        document.add(titulo);
        
        // Rango de fechas
        Paragraph fechas = new Paragraph("Período: " + rangoFechas)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(fechas);
        
        // Línea separadora
        agregarLineaSeparadora(document);
    }

    /**
     * Agrega contenido específico del reporte como lista
     */
    private void agregarContenidoReporte(Document document, List<List<Object>> data, List<String> headers) {
        System.out.println("=== AGREGANDO CONTENIDO AL PDF ===");
        System.out.println("Headers recibidos: " + headers);
        System.out.println("Cantidad de registros: " + data.size());

        if (data.isEmpty()) {
            System.out.println("No hay datos para mostrar");
            Paragraph noData = new Paragraph("No se encontraron datos para el período seleccionado.")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(50)
                    .setFontColor(new DeviceRgb(0, 0, 0)); // Color negro explícito
            document.add(noData);
            return;
        }

        // Mostrar datos como lista en lugar de tabla
        int registroCount = 1;
        for (List<Object> row : data) {
            System.out.println("Procesando registro #" + registroCount + ": " + row);

            // Título del registro con color negro explícito
            Paragraph tituloRegistro = new Paragraph("Registro #" + registroCount)
                    .setBold()
                    .setFontSize(14)
                    .setMarginTop(15)
                    .setMarginBottom(10)
                    .setFontColor(new DeviceRgb(0, 0, 0)); // Color negro explícito
            document.add(tituloRegistro);

            // Crear lista de datos
            com.itextpdf.layout.element.List lista = new com.itextpdf.layout.element.List()
                    .setMarginLeft(20);

            for (int i = 0; i < headers.size() && i < row.size(); i++) {
                String valor = row.get(i) != null ? row.get(i).toString() : "No especificado";
                System.out.println("  " + headers.get(i) + ": " + valor);
                ListItem item = new ListItem();
                // Agregar párrafo con color negro explícito
                Paragraph parrafo = new Paragraph(headers.get(i) + ": " + valor)
                        .setMarginBottom(3)
                        .setFontColor(new DeviceRgb(0, 0, 0)); // Color negro explícito
                item.add(parrafo);
                lista.add(item);
            }

            document.add(lista);

            // Agregar línea separadora después de cada registro
            agregarLineaSeparadora(document);

            registroCount++;
        }

        System.out.println("Contenido agregado exitosamente. Total registros: " + (registroCount - 1));
    }

    /**
     * Agrega una línea separadora amarilla
     */
    private void agregarLineaSeparadora(Document document) {
        Div separator = new Div()
                .setHeight(3)
                .setBackgroundColor(YELLOW_SEPARATOR)
                .setMarginTop(10)
                .setMarginBottom(10);
        document.add(separator);
    }

    /**
     * Obtiene el nombre legible del tipo de reporte
     */
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

    /**
     * Manejador de eventos para header, footer y marca de agua
     */
    private static class HeaderFooterHandler implements IEventHandler {
        private final String tipoReporte;

        public HeaderFooterHandler(String tipoReporte) {
            this.tipoReporte = tipoReporte;
        }

        @Override
        public void handleEvent(Event event) {
            PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
            PdfDocument pdfDoc = docEvent.getDocument();
            PdfPage page = docEvent.getPage();
            PdfCanvas canvas = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), pdfDoc);
            Rectangle pageSize = page.getPageSize();

            try {
                // Header con logo y título
                agregarHeader(canvas, pageSize, tipoReporte);
                
                // Marca de agua
                agregarMarcaDeAgua(canvas, pageSize);
                
                // Footer con número de página y fecha
                agregarFooter(canvas, pageSize, pdfDoc.getPageNumber(page), pdfDoc.getNumberOfPages());
                
            } catch (Exception e) {
                System.err.println("Error al agregar header/footer: " + e.getMessage());
            }
        }

        private void agregarHeader(PdfCanvas canvas, Rectangle pageSize, String tipoReporte) {
            try {
                // Fondo celeste del header (como la página web)
                canvas.setFillColor(CELESTE_HEADER)
                      .rectangle(0, pageSize.getTop() - 80, pageSize.getWidth(), 80)
                      .fill();

                // Intentar cargar y agregar logo (más pequeño)
                try {
                    // Buscar la imagen en el classpath
                    java.io.InputStream logoStream = getClass().getClassLoader()
                            .getResourceAsStream("static/images/Simbolo_SanMiguel.png");

                    if (logoStream != null) {
                        byte[] logoBytes = logoStream.readAllBytes();
                        com.itextpdf.io.image.ImageData logoData =
                                com.itextpdf.io.image.ImageDataFactory.create(logoBytes);

                        // Agregar logo extra ancho en la esquina superior izquierda
                        // Escalar la imagen para que sea extra ancha (160x60 píxeles)
                        canvas.addImageWithTransformationMatrix(logoData, 160, 0, 0, 60,
                                                              10, pageSize.getTop() - 70, false);
                        logoStream.close();
                    }
                } catch (Exception e) {
                    System.err.println("No se pudo cargar el logo: " + e.getMessage());
                }

                // Texto dinámico del tipo de reporte en la derecha
                String tituloReporte = obtenerTituloReporte(tipoReporte);
                canvas.setFillColor(WHITE)
                      .beginText()
                      .setFontAndSize(com.itextpdf.kernel.font.PdfFontFactory.createFont(), 16)
                      .moveText(pageSize.getWidth() - 220, pageSize.getTop() - 45)
                      .showText(tituloReporte)
                      .endText();
            } catch (Exception e) {
                System.err.println("Error al crear header: " + e.getMessage());
            }
        }

        private String obtenerTituloReporte(String tipo) {
            switch (tipo) {
                case "reservas": return "Reporte de Reservas";
                case "ingresos": return "Reporte de Ingresos";
                case "instalaciones": return "Reporte de Instalaciones";
                case "mantenimiento": return "Reporte de Mantenimiento";
                case "asistencias": return "Reporte de Asistencias";
                default: return "Reporte";
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

        private void agregarMarcaDeAgua(PdfCanvas canvas, Rectangle pageSize) {
            try {
                // Intentar cargar y agregar icono como marca de agua
                try {
                    java.io.InputStream iconoStream = getClass().getClassLoader()
                            .getResourceAsStream("static/images/Icono_Municipalidad_SanMiguel.png");

                    if (iconoStream != null) {
                        byte[] iconoBytes = iconoStream.readAllBytes();
                        com.itextpdf.io.image.ImageData iconoData =
                                com.itextpdf.io.image.ImageDataFactory.create(iconoBytes);

                        // Agregar icono como marca de agua en el centro, con máxima transparencia
                        canvas.saveState();
                        canvas.setExtGState(new com.itextpdf.kernel.pdf.extgstate.PdfExtGState().setFillOpacity(0.05f)); // 5% opacidad = 95% transparente
                        // Escalar la imagen para que sea más grande pero muy transparente
                        canvas.addImageWithTransformationMatrix(iconoData, 150, 0, 0, 150,
                                                              pageSize.getWidth() / 2 - 75,
                                                              pageSize.getHeight() / 2 - 75,
                                                              false);
                        canvas.restoreState();
                        iconoStream.close();
                    } else {
                        // Fallback: texto como marca de agua
                        canvas.saveState()
                              .setFillColor(new DeviceRgb(240, 240, 240))
                              .beginText()
                              .setFontAndSize(com.itextpdf.kernel.font.PdfFontFactory.createFont(), 60)
                              .moveText(pageSize.getWidth() / 2 - 150, pageSize.getHeight() / 2)
                              .showText("SAN MIGUEL")
                              .endText()
                              .restoreState();
                    }
                } catch (Exception e) {
                    System.err.println("Error al cargar icono para marca de agua: " + e.getMessage());
                    // Fallback: texto como marca de agua
                    canvas.saveState()
                          .setFillColor(new DeviceRgb(240, 240, 240))
                          .beginText()
                          .setFontAndSize(com.itextpdf.kernel.font.PdfFontFactory.createFont(), 60)
                          .moveText(pageSize.getWidth() / 2 - 150, pageSize.getHeight() / 2)
                          .showText("SAN MIGUEL")
                          .endText()
                          .restoreState();
                }
            } catch (Exception e) {
                System.err.println("Error al crear marca de agua: " + e.getMessage());
            }
        }

        private void agregarFooter(PdfCanvas canvas, Rectangle pageSize, int pageNumber, int totalPages) {
            try {
                // Fondo celeste del footer (como la página web)
                canvas.setFillColor(CELESTE_HEADER)
                      .rectangle(0, 0, pageSize.getWidth(), 40)
                      .fill();

                // Fecha de creación
                String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                canvas.setFillColor(WHITE)
                      .beginText()
                      .setFontAndSize(com.itextpdf.kernel.font.PdfFontFactory.createFont(), 10)
                      .moveText(50, 20)
                      .showText("Generado el " + fecha)
                      .endText();

                // Número de página
                canvas.beginText()
                      .setFontAndSize(com.itextpdf.kernel.font.PdfFontFactory.createFont(), 10)
                      .moveText(pageSize.getWidth() - 100, 20)
                      .showText("Página " + pageNumber + " de " + totalPages)
                      .endText();
            } catch (Exception e) {
                System.err.println("Error al crear footer: " + e.getMessage());
            }
        }
    }
}
