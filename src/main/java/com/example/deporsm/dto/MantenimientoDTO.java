package com.example.deporsm.dto;

import java.time.LocalDateTime;

public class MantenimientoDTO {
    private Integer id;
    private String motivo;
    private String tipo;
    private String descripcion;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private String estado;

    private String instalacionNombre;
    private String instalacionUbicacion;

    // 👇 Este constructor debe ser público y coincidir EXACTAMENTE en ORDEN Y TIPO
    public MantenimientoDTO(Integer id, String motivo, String tipo, String descripcion,
                            LocalDateTime fechaInicio, LocalDateTime fechaFin,
                            String estado, String instalacionNombre, String instalacionUbicacion) {
        this.id = id;
        this.motivo = motivo;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estado = estado;
        this.instalacionNombre = instalacionNombre;
        this.instalacionUbicacion = instalacionUbicacion;
    }

    // Getters obligatorios para que Jackson lo serialice en JSON (si es que los necesitas)
    public Integer getId() { return id; }
    public String getMotivo() { return motivo; }
    public String getTipo() { return tipo; }
    public String getDescripcion() { return descripcion; }
    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public LocalDateTime getFechaFin() { return fechaFin; }
    public String getEstado() { return estado; }
    public String getInstalacionNombre() { return instalacionNombre; }
    public String getInstalacionUbicacion() { return instalacionUbicacion; }
}
