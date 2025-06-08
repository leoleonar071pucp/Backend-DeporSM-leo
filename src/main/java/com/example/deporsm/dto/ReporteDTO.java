package com.example.deporsm.dto;

import java.time.LocalDateTime;

/**
 * DTO para representar los metadatos de un reporte generado
 */
public class ReporteDTO {
    private Integer id;
    private String nombre;
    private String tipo; // reservas, ingresos, instalaciones, mantenimiento
    private String formato; // excel, pdf
    private String rangoFechas;
    private LocalDateTime fechaCreacion;
    private String creadoPor;
    private String tamano;
    private String descripcion;
    private String urlArchivo;
    private Integer instalacionId; // Opcional, si el reporte es específico para una instalación

    public ReporteDTO() {
    }

    public ReporteDTO(Integer id, String nombre, String tipo, String formato, String rangoFechas,
                     LocalDateTime fechaCreacion, String creadoPor, String tamano, String descripcion,
                     String urlArchivo, Integer instalacionId) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.formato = formato;
        this.rangoFechas = rangoFechas;
        this.fechaCreacion = fechaCreacion;
        this.creadoPor = creadoPor;
        this.tamano = tamano;
        this.descripcion = descripcion;
        this.urlArchivo = urlArchivo;
        this.instalacionId = instalacionId;
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

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

    public String getRangoFechas() {
        return rangoFechas;
    }

    public void setRangoFechas(String rangoFechas) {
        this.rangoFechas = rangoFechas;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getCreadoPor() {
        return creadoPor;
    }

    public void setCreadoPor(String creadoPor) {
        this.creadoPor = creadoPor;
    }

    public String getTamano() {
        return tamano;
    }

    public void setTamano(String tamano) {
        this.tamano = tamano;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getUrlArchivo() {
        return urlArchivo;
    }

    public void setUrlArchivo(String urlArchivo) {
        this.urlArchivo = urlArchivo;
    }

    public Integer getInstalacionId() {
        return instalacionId;
    }

    public void setInstalacionId(Integer instalacionId) {
        this.instalacionId = instalacionId;
    }
}
