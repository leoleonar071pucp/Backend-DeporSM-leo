package com.example.deporsm.model;

import jakarta.persistence.*;

import java.sql.Date;
import java.sql.Timestamp;


@Entity
@Table(name = "notificaciones")
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String titulo;
    private String mensaje;
    @Column(name = "tipo")
    private String tipo;

    @Column(name = "leida")
    private Boolean leida;

    @Column(name = "fecha_envio")
    private Timestamp fechaEnvio;






    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

}
