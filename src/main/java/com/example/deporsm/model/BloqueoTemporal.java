package com.example.deporsm.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "bloqueos_temporales")
public class BloqueoTemporal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "instalacion_id", nullable = false)
    private Instalacion instalacion;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "fecha", nullable = false)
    private Date fecha;

    @Column(name = "hora_inicio", nullable = false)
    private Time horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private Time horaFin;

    @Column(name = "token", nullable = false)
    private String token;

    @Column(name = "expiracion", nullable = false)
    private Timestamp expiracion;

    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;

    public BloqueoTemporal(Instalacion instalacion, Usuario usuario, Date fecha, Time horaInicio, Time horaFin) {
        this.instalacion = instalacion;
        this.usuario = usuario;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.token = generateToken();
        this.createdAt = new Timestamp(System.currentTimeMillis());
        // Expiración por defecto: 10 minutos desde la creación
        this.expiracion = Timestamp.valueOf(LocalDateTime.now().plusMinutes(10));
    }

    private String generateToken() {
        return java.util.UUID.randomUUID().toString();
    }

    public boolean isExpirado() {
        return new Timestamp(System.currentTimeMillis()).after(expiracion);
    }
}
