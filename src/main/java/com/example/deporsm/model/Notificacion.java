package com.example.deporsm.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.sql.Timestamp;

@Entity
@Table(name = "notificaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String titulo;
    private String mensaje;

    @Column(name = "tipo")
    private String tipo;

    @Column(name = "leida")
    private Boolean leida = false;

    @Column(name = "fecha_envio")
    private Timestamp fechaEnvio;

    @Column(name = "categoria")
    private String categoria;

    @Column(name = "feedback")
    private String feedback;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
}
