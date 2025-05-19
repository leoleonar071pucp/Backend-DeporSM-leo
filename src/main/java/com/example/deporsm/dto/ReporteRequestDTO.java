package com.example.deporsm.dto;



/**
 * DTO para solicitar la generación de un reporte
 */
public class ReporteRequestDTO {
    private String tipo; // reservas, ingresos, instalaciones, mantenimiento
    private String formato; // excel, pdf
    private String fechaInicio; // Formato: yyyy-MM-dd
    private String fechaFin; // Formato: yyyy-MM-dd
    private Integer instalacionId; // Opcional, si el reporte es específico para una instalación

    public ReporteRequestDTO() {
    }

    public ReporteRequestDTO(String tipo, String formato, String fechaInicio, String fechaFin, Integer instalacionId) {
        this.tipo = tipo;
        this.formato = formato;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.instalacionId = instalacionId;
    }

    // Getters y Setters
    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getFormato() {
        return formato;
    }

    public void setFormato(String formato) {
        this.formato = formato;
    }

    public String getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(String fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public String getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(String fechaFin) {
        this.fechaFin = fechaFin;
    }

    public Integer getInstalacionId() {
        return instalacionId;
    }

    public void setInstalacionId(Integer instalacionId) {
        this.instalacionId = instalacionId;
    }
}
