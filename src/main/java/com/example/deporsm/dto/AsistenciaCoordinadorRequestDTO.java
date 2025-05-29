package com.example.deporsm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsistenciaCoordinadorRequestDTO {
    private Integer coordinadorId;
    private Integer instalacionId;

    // Usar String para evitar problemas de deserialización
    private String fecha; // formato yyyy-MM-dd
    private String horaProgramadaInicio; // formato HH:mm
    private String horaProgramadaFin; // formato HH:mm
    private String horaEntrada; // formato HH:mm
    private String estadoEntrada; // usar String para evitar problemas con enum
    private String horaSalida; // formato HH:mm
    private String estadoSalida; // usar String para evitar problemas con enum
    private String ubicacion;
    private String notas;
}
