package com.example.deporsm.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidad para almacenar metadatos de reportes generados
 */
@Getter
@Setter
@Entity
@Table(name = "reportes")
public class Reporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "tipo", nullable = false)
    private String tipo; // reservas, ingresos, instalaciones, mantenimiento

    @Column(name = "formato", nullable = false)
    private String formato; // excel, pdf

    @Column(name = "rango_fechas", nullable = false)
    private String rangoFechas;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "tamano")
    private String tamano;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "url_archivo", nullable = false)
    private String urlArchivo;

    @ManyToOne
    @JoinColumn(name = "instalacion_id")
    private Instalacion instalacion; // Opcional, si el reporte es específico para una instalación

    // Constructor por defecto
    public Reporte() {
    }

    // Constructor con parámetros
    public Reporte(String nombre, String tipo, String formato, String rangoFechas,
                  LocalDateTime fechaCreacion, Usuario usuario, String tamano,
                  String descripcion, String urlArchivo, Instalacion instalacion) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.formato = formato;
        this.rangoFechas = rangoFechas;
        this.fechaCreacion = fechaCreacion;
        this.usuario = usuario;
        this.tamano = tamano;
        this.descripcion = descripcion;
        this.urlArchivo = urlArchivo;
        this.instalacion = instalacion;
    }
}
