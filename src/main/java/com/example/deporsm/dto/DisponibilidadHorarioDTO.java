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
    public static class RangoHorarioDTO {
        private Time horaInicio;
        private Time horaFin;
        private boolean bloqueadoTemporalmente = false;

        public RangoHorarioDTO(Time horaInicio, Time horaFin) {
            this.horaInicio = horaInicio;
            this.horaFin = horaFin;
            this.bloqueadoTemporalmente = false;
        }

        public RangoHorarioDTO(Time horaInicio, Time horaFin, boolean bloqueadoTemporalmente) {
            this.horaInicio = horaInicio;
            this.horaFin = horaFin;
            this.bloqueadoTemporalmente = bloqueadoTemporalmente;
        }
    }
}
