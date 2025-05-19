package com.example.deporsm.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

@Entity
@Table(name = "asistencias_coordinadores")
@Getter
@Setter
public class AsistenciaCoordinador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "coordinador_id")
    private Usuario coordinador;

    @ManyToOne
    @JoinColumn(name = "instalacion_id")
    private Instalacion instalacion;

    @Column(name = "fecha")
    private Date fecha;

    @Column(name = "hora_programada_inicio")
    private Time horaProgramadaInicio;

    @Column(name = "hora_programada_fin")
    private Time horaProgramadaFin;

    @Column(name = "hora_entrada")
    private Time horaEntrada;

    @Column(name = "estado_entrada")
    @Enumerated(EnumType.STRING)
    private EstadoAsistencia estadoEntrada;

    @Column(name = "hora_salida")
    private Time horaSalida;

    @Column(name = "estado_salida")
    @Enumerated(EnumType.STRING)
    private EstadoAsistencia estadoSalida;

    @Column(name = "ubicacion")
    private String ubicacion;

    @Column(name = "notas")
    private String notas;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;
    
    // Enum para los estados de asistencia
    public enum EstadoAsistencia {
        @JsonProperty("a-tiempo") A_TIEMPO("a-tiempo"),
        @JsonProperty("tarde") TARDE("tarde"),
        @JsonProperty("no-asistio") NO_ASISTIO("no-asistio"),
        @JsonProperty("pendiente") PENDIENTE("pendiente");
        
        private final String value;
        
        EstadoAsistencia(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
        
        @Override
        public String toString() {
            return value;
        }
    }
}
