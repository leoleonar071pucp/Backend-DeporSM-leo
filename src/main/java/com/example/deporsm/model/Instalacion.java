package com.example.deporsm.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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

    @Column(name = "imagen_url")
    private String imagenUrl;

    @Column(name = "precio")
    private float precio;

    @Column(name = "contacto_numero")
    private String contactoNumero;

    private Boolean activo;

    @Column(name = "requiere_mantenimiento")
    private Boolean requiereMantenimiento = false;

    @Column(name = "latitud")
    private Double latitud;

    @Column(name = "longitud")
    private Double longitud;

    @Column(name = "radio_validacion")
    private Integer radioValidacion = 100;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    // Relaciones con otras entidades
    @OneToMany(mappedBy = "instalacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Reserva> reservas;

    @OneToMany(mappedBy = "instalacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Observacion> observaciones;

    @OneToMany(mappedBy = "instalacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<MantenimientoInstalacion> mantenimientos;
      // Nuevas relaciones
    @OneToMany(mappedBy = "instalacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<CaracteristicaInstalacion> caracteristicas;

    @OneToMany(mappedBy = "instalacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<ComodidadInstalacion> comodidades;

    @OneToMany(mappedBy = "instalacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<ReglaInstalacion> reglas;

    @OneToMany(mappedBy = "instalacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<HorarioDisponible> horariosDisponibles;

    @OneToMany(mappedBy = "instalacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<CoordinadorInstalacion> coordinadores;
}
