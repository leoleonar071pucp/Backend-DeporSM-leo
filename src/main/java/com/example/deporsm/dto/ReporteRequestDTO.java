package com.example.deporsm.dto;



/**
 * DTO para solicitar la generación de un reporte
 */
public class ReporteRequestDTO {
    private String tipo; // reservas, ingresos, instalaciones, mantenimiento, asistencias
    private String formato; // excel, pdf
    private String fechaInicio; // Formato: yyyy-MM-dd
    private String fechaFin; // Formato: yyyy-MM-dd
    private Integer instalacionId; // Opcional, si el reporte es específico para una instalación

    // Campos específicos para filtros de asistencias
    private String coordinadorNombre;
    private String instalacionNombre;
    private String estadoEntrada;
    private String estadoSalida;
    private String filtrosTexto; // Para incluir en el nombre del archivo
    private String fechasTexto; // Para incluir fechas específicas en el nombre

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

    public String getCoordinadorNombre() {
        return coordinadorNombre;
    }

    public void setCoordinadorNombre(String coordinadorNombre) {
        this.coordinadorNombre = coordinadorNombre;
    }

    public String getInstalacionNombre() {
        return instalacionNombre;
    }

    public void setInstalacionNombre(String instalacionNombre) {
        this.instalacionNombre = instalacionNombre;
    }

    public String getEstadoEntrada() {
        return estadoEntrada;
    }

    public void setEstadoEntrada(String estadoEntrada) {
        this.estadoEntrada = estadoEntrada;
    }

    public String getEstadoSalida() {
        return estadoSalida;
    }

    public void setEstadoSalida(String estadoSalida) {
        this.estadoSalida = estadoSalida;
    }

    public String getFiltrosTexto() {
        return filtrosTexto;
    }

    public void setFiltrosTexto(String filtrosTexto) {
        this.filtrosTexto = filtrosTexto;
    }

    public String getFechasTexto() {
        return fechasTexto;
    }

    public void setFechasTexto(String fechasTexto) {
        this.fechasTexto = fechasTexto;
    }
}
