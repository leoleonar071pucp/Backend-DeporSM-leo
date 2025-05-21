// src/main/java/com/example/deporsm/dto/ObservacionDTO.java
package com.example.deporsm.dto;

public class ObservacionDTO {
    private Integer idObservacion;
    private String instalacion;
    private String titulo;
    private String descripcion;
    private String coordinador;
    private String fecha;
    private String estado;
    private String prioridad;
    private String ubicacion;
    private String fotosUrl;    // Constructor que debe coincidir ORDEN Y TIPO
    public ObservacionDTO(Integer idObservacion, String instalacion, String titulo, String descripcion,
                          String coordinador, String fecha, String estado, String prioridad,
                          String ubicacion, String fotosUrl) {
        this.idObservacion = idObservacion;
        this.instalacion = instalacion;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.coordinador = coordinador;
        this.fecha = fecha;
        this.estado = estado;
        this.prioridad = prioridad;
        this.ubicacion = ubicacion;
        this.fotosUrl = fotosUrl;
    }

    // Getters necesarios
    public Integer getIdObservacion() {
        return idObservacion;
    }

    public String getInstalacion() {
        return instalacion;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getCoordinador() {
        return coordinador;
    }

    public String getFecha() {
        return fecha;
    }

    public String getEstado() {
        return estado;
    }    public String getPrioridad() {
        return prioridad;
    }
    public String getUbicacion() {
        return ubicacion;
    }

    public String getFotosUrl() {
        return fotosUrl;
    }
}
