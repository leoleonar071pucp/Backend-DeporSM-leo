package com.example.deporsm.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * DTO para devolver información de mantenimiento de una instalación
 */
public class MantenimientoInfoDTO {
    private String ultimoMantenimiento;
    private String proximoMantenimiento;
    private Boolean tieneMantenimientoCompletado;
    private Boolean tieneMantenimientoProgramado;
    private Boolean tieneMantenimientoActivo;
    private Boolean enMantenimiento;

    public MantenimientoInfoDTO() {
        this.tieneMantenimientoCompletado = false;
        this.tieneMantenimientoProgramado = false;
        this.tieneMantenimientoActivo = false;
        this.enMantenimiento = false;
    }

    public String getUltimoMantenimiento() {
        return ultimoMantenimiento;
    }

    public void setUltimoMantenimiento(String ultimoMantenimiento) {
        this.ultimoMantenimiento = ultimoMantenimiento;
        this.tieneMantenimientoCompletado = (ultimoMantenimiento != null);
    }

    public void setUltimoMantenimiento(LocalDateTime fechaFin) {
        if (fechaFin != null) {
            this.ultimoMantenimiento = fechaFin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            this.tieneMantenimientoCompletado = true;
        } else {
            this.ultimoMantenimiento = null;
            this.tieneMantenimientoCompletado = false;
        }
    }

    public String getProximoMantenimiento() {
        return proximoMantenimiento;
    }

    public void setProximoMantenimiento(String proximoMantenimiento) {
        this.proximoMantenimiento = proximoMantenimiento;
        this.tieneMantenimientoProgramado = (proximoMantenimiento != null);
    }

    public void setProximoMantenimiento(LocalDateTime fechaInicio) {
        if (fechaInicio != null) {
            this.proximoMantenimiento = fechaInicio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            this.tieneMantenimientoProgramado = true;
        } else {
            this.proximoMantenimiento = null;
            this.tieneMantenimientoProgramado = false;
        }
    }

    public Boolean getTieneMantenimientoCompletado() {
        return tieneMantenimientoCompletado;
    }

    public void setTieneMantenimientoCompletado(Boolean tieneMantenimientoCompletado) {
        this.tieneMantenimientoCompletado = tieneMantenimientoCompletado;
    }

    public Boolean getTieneMantenimientoProgramado() {
        return tieneMantenimientoProgramado;
    }

    public void setTieneMantenimientoProgramado(Boolean tieneMantenimientoProgramado) {
        this.tieneMantenimientoProgramado = tieneMantenimientoProgramado;
    }

    public Boolean getTieneMantenimientoActivo() {
        return tieneMantenimientoActivo;
    }

    public void setTieneMantenimientoActivo(Boolean tieneMantenimientoActivo) {
        this.tieneMantenimientoActivo = tieneMantenimientoActivo;
    }

    public Boolean getEnMantenimiento() {
        return enMantenimiento;
    }

    public void setEnMantenimiento(Boolean enMantenimiento) {
        this.enMantenimiento = enMantenimiento;
    }
}
