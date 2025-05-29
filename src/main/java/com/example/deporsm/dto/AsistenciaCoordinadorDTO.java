package com.example.deporsm.dto;

import com.example.deporsm.model.AsistenciaCoordinador.EstadoAsistencia;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.sql.Time;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsistenciaCoordinadorDTO {
    private Integer id;
    private Integer coordinadorId;
    private String nombreCoordinador;
    private Integer instalacionId;
    private String nombreInstalacion;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date fecha;

    @JsonFormat(pattern = "HH:mm")
    private Time horaProgramadaInicio;

    @JsonFormat(pattern = "HH:mm")
    private Time horaProgramadaFin;

    @JsonFormat(pattern = "HH:mm")
    private Time horaEntrada;

    private EstadoAsistencia estadoEntrada;

    @JsonFormat(pattern = "HH:mm")
    private Time horaSalida;

    private EstadoAsistencia estadoSalida;
    private String ubicacion;
    private String notas;
}
