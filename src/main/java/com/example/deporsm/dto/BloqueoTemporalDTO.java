package com.example.deporsm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BloqueoTemporalDTO {
    private Integer instalacionId;
    private String fecha;           // Cambiar a String para evitar problemas de zona horaria
    private String horaInicio;      // Cambiar a String para evitar problemas de zona horaria
    private String horaFin;         // Cambiar a String para evitar problemas de zona horaria
    private String token;
}
