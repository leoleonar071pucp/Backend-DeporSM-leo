package com.example.deporsm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HorarioCoordinadorRequestDTO {
    private Integer instalacionId;
    private String diaSemana;
    private String horaInicio;
    private String horaFin;
}
