package com.example.deporsm.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Time;
import java.sql.Timestamp;


@Entity
@Table(name = "asistencia")
@Getter
@Setter
public class Asistencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "reserva_id")
    private Reserva reserva;

    @ManyToOne
    @JoinColumn(name = "registrado_por")
    private Usuario usuario;

    @Column(name = "asistentes_reales")
    private Integer asistentesReales;

    @Column(name = "hora_entrada")
    private Time registroEntrada;

    @Column(name = "hora_salida")
    private Time registroSalida;

    @Column(name = "observaciones")
    private String observaciones; // Usar este campo en lugar de 'motivoFalta' o 'comentarioExtra'

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;
}
