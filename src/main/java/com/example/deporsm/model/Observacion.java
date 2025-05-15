package com.example.deporsm.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Table(name = "observaciones")
public class Observacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;    @ManyToOne
    @JoinColumn(name = "instalacion_id")
    @JsonIgnore
    private Instalacion instalacion;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    private String titulo;

    private String descripcion;

    private String estado;

    private String prioridad;

    @Column(name = "fecha_resolucion")
    private Timestamp fechaResolucion;

    @ManyToOne
    @JoinColumn(name = "resuelto_por ")
    private Usuario resueltoPor;

    @Column(name = "comentario_resolucion", columnDefinition = "TEXT")
    private String comentarioResolucion;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;
}
