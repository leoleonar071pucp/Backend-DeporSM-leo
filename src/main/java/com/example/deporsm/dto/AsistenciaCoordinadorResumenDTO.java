package com.example.deporsm.dto;

import com.example.deporsm.model.AsistenciaCoordinador.EstadoAsistencia;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsistenciaCoordinadorResumenDTO {
    private Integer coordinadorId;
    private String nombreCoordinador;
    private long totalAsistencias;
    private long atiempo;
    private long tarde;
    private long noAsistio;
    private long pendiente;
    private Map<String, List<AsistenciaCoordinadorDTO>> asistenciasPorFecha;
}
