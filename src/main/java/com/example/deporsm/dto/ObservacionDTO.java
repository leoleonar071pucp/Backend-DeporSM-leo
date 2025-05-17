// src/main/java/com/example/deporsm/dto/ObservacionDTO.java
package com.example.deporsm.dto;

public class ObservacionDTO {
    private Integer idObservacion;
    private String instalacion;
    private String descripcion;
    private String coordinador;
    private String fecha;
    private String estado;
    private String prioridad;
    private String ubicacion;

    // Constructor que debe coincidir ORDEN Y TIPO
    public ObservacionDTO(Integer idObservacion, String instalacion, String descripcion,
                          String coordinador, String fecha, String estado, String prioridad, 
                          String ubicacion) {
        this.idObservacion = idObservacion;
        this.instalacion = instalacion;
        this.descripcion = descripcion;
        this.coordinador = coordinador;
        this.fecha = fecha;
        this.estado = estado;
        this.prioridad = prioridad;
        this.ubicacion = ubicacion;
    }

    // Getters necesarios
    public Integer getIdObservacion() {
        return idObservacion;
    }

    public String getInstalacion() {
        return instalacion;
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
}
