package com.example.deporsm.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "configuracion_general")
public class ConfiguracionGeneral {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre_sitio", length = 255)
    private String nombreSitio;

    @Column(name = "descripcion_sitio", length = 1000)
    private String descripcionSitio;

    @Column(name = "telefono_contacto", length = 50)
    private String telefonoContacto;
    
    @Column(name = "email_contacto", length = 255)
    private String emailContacto;

    @Column(name = "max_reservas_por_usuario")
    private Integer maxReservasPorUsuario;

    @Column(name = "limite_tiempo_cancelacion")
    private Integer limiteTiempoCancelacion;

    @Column(name = "modo_mantenimiento")
    private Boolean modoMantenimiento;

    @Column(name = "registro_habilitado")
    private Boolean registroHabilitado;

    @Column(name = "reservas_habilitadas")
    private Boolean reservasHabilitadas;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructor por defecto
    public ConfiguracionGeneral() {
        // Valores por defecto
        this.nombreSitio = "DeporSM - Sistema de Reservas Deportivas";
        this.descripcionSitio = "Sistema de reserva de canchas y servicios deportivos para la Municipalidad de San Miguel.";
        this.telefonoContacto = "987-654-321";
        this.emailContacto = "deportes@munisanmiguel.gob.pe";
        this.maxReservasPorUsuario = 3;
        this.limiteTiempoCancelacion = 48;
        this.modoMantenimiento = false;
        this.registroHabilitado = true;
        this.reservasHabilitadas = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombreSitio() {
        return nombreSitio;
    }

    public void setNombreSitio(String nombreSitio) {
        this.nombreSitio = nombreSitio;
    }

    public String getDescripcionSitio() {
        return descripcionSitio;
    }

    public void setDescripcionSitio(String descripcionSitio) {
        this.descripcionSitio = descripcionSitio;
    }

    public String getTelefonoContacto() {
        return telefonoContacto;
    }

    public void setTelefonoContacto(String telefonoContacto) {
        this.telefonoContacto = telefonoContacto;
    }

    public String getEmailContacto() {
        return emailContacto;
    }

    public void setEmailContacto(String emailContacto) {
        this.emailContacto = emailContacto;
    }

    public Integer getMaxReservasPorUsuario() {
        return maxReservasPorUsuario;
    }

    public void setMaxReservasPorUsuario(Integer maxReservasPorUsuario) {
        this.maxReservasPorUsuario = maxReservasPorUsuario;
    }

    public Integer getLimiteTiempoCancelacion() {
        return limiteTiempoCancelacion;
    }

    public void setLimiteTiempoCancelacion(Integer limiteTiempoCancelacion) {
        this.limiteTiempoCancelacion = limiteTiempoCancelacion;
    }

    public Boolean getModoMantenimiento() {
        return modoMantenimiento;
    }

    public void setModoMantenimiento(Boolean modoMantenimiento) {
        this.modoMantenimiento = modoMantenimiento;
    }

    public Boolean getRegistroHabilitado() {
        return registroHabilitado;
    }

    public void setRegistroHabilitado(Boolean registroHabilitado) {
        this.registroHabilitado = registroHabilitado;
    }

    public Boolean getReservasHabilitadas() {
        return reservasHabilitadas;
    }

    public void setReservasHabilitadas(Boolean reservasHabilitadas) {
        this.reservasHabilitadas = reservasHabilitadas;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
