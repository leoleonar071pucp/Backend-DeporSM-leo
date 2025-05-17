// src/main/java/com/example/deporsm/dto/ObservacionRequestDTO.java
package com.example.deporsm.dto;

import java.util.List;

public class ObservacionRequestDTO {
    private Integer instalacionId;
    private Integer usuarioId;
    private String titulo;
    private String descripcion;
    private String prioridad;
    private String ubicacionLat;
    private String ubicacionLng;
    private List<String> fotos;
    
    // Getters and setters
    public Integer getInstalacionId() {
        return instalacionId;
    }
    
    public void setInstalacionId(Integer instalacionId) {
        this.instalacionId = instalacionId;
    }
    
    public Integer getUsuarioId() {
        return usuarioId;
    }
    
    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }
    
    public String getTitulo() {
        return titulo;
    }
    
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public String getPrioridad() {
        return prioridad;
    }
    
    public void setPrioridad(String prioridad) {
        this.prioridad = prioridad;
    }
    
    public String getUbicacionLat() {
        return ubicacionLat;
    }
    
    public void setUbicacionLat(String ubicacionLat) {
        this.ubicacionLat = ubicacionLat;
    }
    
    public String getUbicacionLng() {
        return ubicacionLng;
    }
    
    public void setUbicacionLng(String ubicacionLng) {
        this.ubicacionLng = ubicacionLng;
    }
    
    public List<String> getFotos() {
        return fotos;
    }
    
    public void setFotos(List<String> fotos) {
        this.fotos = fotos;
    }
}
