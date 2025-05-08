package com.example.deporsm.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "instalaciones")
public class Instalacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nombre;

    private String descripcion;

    private String ubicacion;

    private String tipo;

    private Integer capacidad;

    @Column(name = "horario_apertura")
    private Time horarioApertura;

    @Column(name = "horario_cierre")
    private Time horarioCierre;

    @Column(name = "imagen_url")
    private String imagenUrl;

    @Column(name = "precio")
    private float precio;

    private Boolean activo;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    // Relaciones con otras entidades
    @OneToMany(mappedBy = "instalacion")
    @JsonIgnore
    private List<Reserva> reservas;

    @OneToMany(mappedBy = "instalacion")
    @JsonIgnore
    private List<Observacion> observaciones;
}
