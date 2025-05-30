package com.example.deporsm.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "mantenimiento_instalaciones")
public class MantenimientoInstalacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @JsonIgnore
    @JoinColumn(name = "instalacion_id", nullable = false)
    private Instalacion instalacion;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDateTime fechaFin;

    @Column(nullable = false)
    private String motivo;

    @Column(name = "tipo")
    private String tipo; // valores posibles: preventivo, correctivo, mejora

    private String descripcion;

    @Column(name = "estado")
    private String estado; // valores posibles: programado, en-progreso, completado, cancelado

    @Column(name = "afecta_disponibilidad")
    private Boolean afectaDisponibilidad; // indica si el mantenimiento afecta la disponibilidad de la instalación


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registrado_por", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})

    private Usuario registradoPor;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
