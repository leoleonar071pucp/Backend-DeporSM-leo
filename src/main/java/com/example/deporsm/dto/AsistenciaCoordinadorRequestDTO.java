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
public class AsistenciaCoordinadorRequestDTO {
    private Integer coordinadorId;
    private Integer instalacionId;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date fecha;
    
    @JsonFormat(pattern = "HH:mm:ss")
    private Time horaProgramadaInicio;
    
    @JsonFormat(pattern = "HH:mm:ss")
    private Time horaProgramadaFin;
    
    @JsonFormat(pattern = "HH:mm:ss")
    private Time horaEntrada;
    
    private EstadoAsistencia estadoEntrada;
    
    @JsonFormat(pattern = "HH:mm:ss")
    private Time horaSalida;
    
    private EstadoAsistencia estadoSalida;
    private String ubicacion;
    private String notas;
}
