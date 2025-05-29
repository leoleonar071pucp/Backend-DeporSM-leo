package com.example.deporsm.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
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
    @Convert(converter = EstadoAsistenciaConverter.class)
    private EstadoAsistencia estadoEntrada;

    @Column(name = "hora_salida")
    private Time horaSalida;

    @Column(name = "estado_salida")
    @Convert(converter = EstadoAsistenciaConverter.class)
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

        // Método para convertir desde String
        @JsonCreator
        public static EstadoAsistencia fromString(String value) {
            if (value == null) return PENDIENTE;

            for (EstadoAsistencia estado : EstadoAsistencia.values()) {
                if (estado.value.equalsIgnoreCase(value)) {
                    return estado;
                }
            }
            return PENDIENTE; // Valor por defecto
        }

        // Método para serializar a String
        @JsonValue
        public String toValue() {
            return this.value;
        }
    }

    // Convertidor JPA para EstadoAsistencia
    @Converter
    public static class EstadoAsistenciaConverter implements AttributeConverter<EstadoAsistencia, String> {

        @Override
        public String convertToDatabaseColumn(EstadoAsistencia attribute) {
            if (attribute == null) {
                return null;
            }
            return attribute.getValue();
        }

        @Override
        public EstadoAsistencia convertToEntityAttribute(String dbData) {
            if (dbData == null) {
                return null;
            }
            return EstadoAsistencia.fromString(dbData);
        }
    }
}
