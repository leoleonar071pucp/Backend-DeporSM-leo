package com.example.deporsm.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Utilidad para generar archivos Excel usando Apache POI
 */
@Component
public class ExcelGenerator {

    /**
     * Genera un archivo Excel con los datos proporcionados
     * @param headers Lista de encabezados de columnas
     * @param data Lista de filas de datos
     * @param sheetName Nombre de la hoja
     * @return Array de bytes del archivo Excel
     * @throws IOException Si hay error al generar el archivo
     */
    public byte[] generateExcel(List<String> headers, List<List<Object>> data, String sheetName) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(sheetName);

            // Crear estilo para encabezados
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);

            // Crear estilo para datos
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);

            // Crear fila de encabezados
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
            }

            // Crear filas de datos
            for (int i = 0; i < data.size(); i++) {
                Row row = sheet.createRow(i + 1);
                List<Object> rowData = data.get(i);
                
                for (int j = 0; j < rowData.size() && j < headers.size(); j++) {
                    Cell cell = row.createCell(j);
                    Object value = rowData.get(j);
                    
                    if (value != null) {
                        if (value instanceof Number) {
                            cell.setCellValue(((Number) value).doubleValue());
                        } else {
                            cell.setCellValue(value.toString());
                        }
                    } else {
                        cell.setCellValue("");
                    }
                    
                    cell.setCellStyle(dataStyle);
                }
            }

            // Ajustar ancho de columnas automáticamente
            for (int i = 0; i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
                // Establecer un ancho mínimo y máximo
                int currentWidth = sheet.getColumnWidth(i);
                if (currentWidth < 2000) {
                    sheet.setColumnWidth(i, 2000);
                } else if (currentWidth > 8000) {
                    sheet.setColumnWidth(i, 8000);
                }
            }

            // Convertir a array de bytes
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                workbook.write(outputStream);
                return outputStream.toByteArray();
            }
        }
    }

    /**
     * Genera un archivo Excel con múltiples hojas
     * @param sheetsData Mapa con nombre de hoja como clave y datos como valor
     * @return Array de bytes del archivo Excel
     * @throws IOException Si hay error al generar el archivo
     */
    public byte[] generateMultiSheetExcel(java.util.Map<String, SheetData> sheetsData) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            
            // Crear estilos una vez para reutilizar
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            for (java.util.Map.Entry<String, SheetData> entry : sheetsData.entrySet()) {
                String sheetName = entry.getKey();
                SheetData sheetData = entry.getValue();
                
                Sheet sheet = workbook.createSheet(sheetName);
                createSheetContent(sheet, sheetData.getHeaders(), sheetData.getData(), headerStyle, dataStyle);
            }

            // Convertir a array de bytes
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                workbook.write(outputStream);
                return outputStream.toByteArray();
            }
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        return headerStyle;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        return dataStyle;
    }

    private void createSheetContent(Sheet sheet, List<String> headers, List<List<Object>> data, 
                                  CellStyle headerStyle, CellStyle dataStyle) {
        // Crear fila de encabezados
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers.get(i));
            cell.setCellStyle(headerStyle);
        }

        // Crear filas de datos
        for (int i = 0; i < data.size(); i++) {
            Row row = sheet.createRow(i + 1);
            List<Object> rowData = data.get(i);
            
            for (int j = 0; j < rowData.size() && j < headers.size(); j++) {
                Cell cell = row.createCell(j);
                Object value = rowData.get(j);
                
                if (value != null) {
                    if (value instanceof Number) {
                        cell.setCellValue(((Number) value).doubleValue());
                    } else {
                        cell.setCellValue(value.toString());
                    }
                } else {
                    cell.setCellValue("");
                }
                
                cell.setCellStyle(dataStyle);
            }
        }

        // Ajustar ancho de columnas
        for (int i = 0; i < headers.size(); i++) {
            sheet.autoSizeColumn(i);
            int currentWidth = sheet.getColumnWidth(i);
            if (currentWidth < 2000) {
                sheet.setColumnWidth(i, 2000);
            } else if (currentWidth > 8000) {
                sheet.setColumnWidth(i, 8000);
            }
        }
    }

    /**
     * Clase auxiliar para datos de hoja
     */
    public static class SheetData {
        private List<String> headers;
        private List<List<Object>> data;

        public SheetData(List<String> headers, List<List<Object>> data) {
            this.headers = headers;
            this.data = data;
        }

        public List<String> getHeaders() {
            return headers;
        }

        public List<List<Object>> getData() {
            return data;
        }
    }
}
