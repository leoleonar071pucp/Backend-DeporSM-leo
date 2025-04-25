package com.example.deporsm.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Table(name = "log_actividades") // 🔧 Nombre correcto de la tabla en la base de datos
public class LogActividad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "accion", nullable = false)
    private String accion;

    @Column(name = "recurso")
    private String recurso;

    @Column(name = "detalles", columnDefinition = "TEXT")
    private String detalles;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "device")
    private String device;

    @Column(name = "estado")
    private String estado; // Puede mapearse como String para ENUM('success', 'failed')

    @Column(name = "created_at")
    private Timestamp createdAt;
}
