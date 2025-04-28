package com.example.deporsm.dto;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

public class ReservaListDTO {

    private Integer id;
    private String usuarioNombre;
    private String instalacionNombre;
    private Date fecha;
    private Time horaInicio;
    private Time horaFin;
    private String estado;
    private String estadoPago;

    // Constructor con parámetros para usarlo en la consulta
    public ReservaListDTO(Integer id, String usuarioNombre, String instalacionNombre, Date fecha,
                          Time horaInicio, Time horaFin, String estado, String estadoPago) {
        this.id = id;
        this.usuarioNombre = usuarioNombre;
        this.instalacionNombre = instalacionNombre;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.estado = estado;
        this.estadoPago = estadoPago;
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsuarioNombre() {
        return usuarioNombre;
    }

    public void setUsuarioNombre(String usuarioNombre) {
        this.usuarioNombre = usuarioNombre;
    }

    public String getInstalacionNombre() {
        return instalacionNombre;
    }

    public void setInstalacionNombre(String instalacionNombre) {
        this.instalacionNombre = instalacionNombre;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public Time getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(Time horaInicio) {
        this.horaInicio = horaInicio;
    }

    public Time getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(Time horaFin) {
        this.horaFin = horaFin;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getEstadoPago() {
        return estadoPago;
    }

    public void setEstadoPago(String estadoPago) {
        this.estadoPago = estadoPago;
    }

    // Método para generar un rango de hora en formato "HH:mm - HH:mm"
    public String getHora() {
        return this.horaInicio.toString() + " - " + this.horaFin.toString();
    }
}
