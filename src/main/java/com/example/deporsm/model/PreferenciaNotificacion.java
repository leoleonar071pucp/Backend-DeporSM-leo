package com.example.deporsm.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Table(name = "preferencias_notificaciones") // ✅ Nombre correcto de la tabla
public class PreferenciaNotificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    private Boolean email;
    private Boolean reservas;
    private Boolean promociones;
    private Boolean mantenimiento;

    @Enumerated(EnumType.STRING)
    private FrecuenciaNotificacion frecuencia;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    public enum FrecuenciaNotificacion {
        @Enumerated(EnumType.STRING)
        @Column(name = "frecuencia")
        tiempo_real, diario, semanal
    }
}
