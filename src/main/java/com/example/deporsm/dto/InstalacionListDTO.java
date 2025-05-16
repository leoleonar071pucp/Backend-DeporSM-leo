package com.example.deporsm.dto;

import java.sql.Time;
import java.sql.Timestamp;

public class InstalacionListDTO {
    private Integer id;
    private String nombre;
    private String descripcion;
    private String ubicacion;
    private String tipo;
    private Integer capacidad;
    private Time horarioApertura;    private Time horarioCierre;
    private String imagenUrl;
    private float precio;
    private Boolean activo;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public InstalacionListDTO(Integer id, String nombre, String descripcion, String ubicacion,                          String tipo, Integer capacidad, Time horarioApertura, Time horarioCierre,
                          String imagenUrl, float precio, Boolean activo, 
                          Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.ubicacion = ubicacion;
        this.tipo = tipo;
        this.capacidad = capacidad;
        this.horarioApertura = horarioApertura;        this.horarioCierre = horarioCierre;
        this.imagenUrl = imagenUrl;
        this.precio = precio;
        this.activo = activo;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public String getUbicacion() { return ubicacion; }
    public String getTipo() { return tipo; }
    public Integer getCapacidad() { return capacidad; }
    public Time getHorarioApertura() { return horarioApertura; }    public Time getHorarioCierre() { return horarioCierre; }
    public String getImagenUrl() { return imagenUrl; }
    public float getPrecio() { return precio; }
    public Boolean getActivo() { return activo; }
    public Timestamp getCreatedAt() { return createdAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }
}
