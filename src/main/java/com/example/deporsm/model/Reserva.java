package com.example.deporsm.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "Reservas")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idReserva")
    private Integer idReserva;

    @Column(name = "dniUsuario")
    private String dniUsuario;

    @Column(name = "id_instalacion")
    private Integer idInstalacion;

    private LocalDate fecha;

    @Column(name = "hora_inicio")
    private LocalTime horaInicio;

    @Column(name = "hora_fin")
    private LocalTime horaFin;

    @Enumerated(EnumType.STRING)
    private Estado estado;

    @Enumerated(EnumType.STRING)
    @Column(name = "pagoState")
    private PagoState pagoState;

    @Column(name = "nota_reserva")
    private String notaReserva;

    // ✅ Enums embebidos (pueden ir fuera también)
    public enum Estado {
        pendiente, confirmada, cancelada
    }

    public enum PagoState {
        pendiente, validado, rechazado
    }

    // Getters y Setters...
}
