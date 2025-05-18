package com.example.deporsm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;
import java.sql.Time;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BloqueoTemporalDTO {
    private Integer instalacionId;
    private Date fecha;
    private Time horaInicio;
    private Time horaFin;
    private String token;
}
