package com.example.deporsm.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Table(name = "reservas")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "instalacion_id")
    private Instalacion instalacion;

    @Column(name="fecha")
    private Date fecha;

    @Column(name = "hora_inicio")
    private Time horaInicio;

    @Column(name = "hora_fin")
    private Time horaFin;

    @Column(name="estado")
    private String estado;

    @Column(name="motivo")
    private String motivo;

    @Column(name = "numero_asistentes")
    private Integer numeroAsistentes;

    @Column(name="comentarios")
    private String comentarios;

    @Column(name = "estado_pago") // Nuevo campo
    private String estadoPago;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;


}
