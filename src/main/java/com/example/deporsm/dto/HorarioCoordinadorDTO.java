package com.example.deporsm.dto;

import lombok.Data;

@Data
public class HorarioCoordinadorDTO {
    private Integer id;
    private Integer coordinadorInstalacionId;
    private String diaSemana;
    private String horaInicio;
    private String horaFin;
    private String instalacionNombre;
    private Integer instalacionId;
}
