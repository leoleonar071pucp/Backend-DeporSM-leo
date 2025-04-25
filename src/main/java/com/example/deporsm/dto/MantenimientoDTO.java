package com.example.deporsm.dto;

import java.time.LocalDateTime;

public class MantenimientoDTO {
    private Integer id;
    private String motivo;
    private String descripcion;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;

    private String instalacionNombre;
    private String instalacionUbicacion;

    // 👇 Este constructor debe ser público y coincidir EXACTAMENTE en ORDEN Y TIPO
    public MantenimientoDTO(Integer id, String motivo, String descripcion,
                            LocalDateTime fechaInicio, LocalDateTime fechaFin,
                            String instalacionNombre, String instalacionUbicacion) {
        this.id = id;
        this.motivo = motivo;
        this.descripcion = descripcion;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.instalacionNombre = instalacionNombre;
        this.instalacionUbicacion = instalacionUbicacion;
    }

    // Getters obligatorios para que Jackson lo serialice en JSON (si es que los necesitas)
    public Integer getId() { return id; }
    public String getMotivo() { return motivo; }
    public String getDescripcion() { return descripcion; }
    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public LocalDateTime getFechaFin() { return fechaFin; }
    public String getInstalacionNombre() { return instalacionNombre; }
    public String getInstalacionUbicacion() { return instalacionUbicacion; }
}
