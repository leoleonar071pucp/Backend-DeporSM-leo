package com.example.deporsm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;
import java.sql.Time;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DisponibilidadHorarioDTO {
    private Date fecha;
    private List<RangoHorarioDTO> horariosDisponibles;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RangoHorarioDTO {
        private Time horaInicio;
        private Time horaFin;
    }
}
