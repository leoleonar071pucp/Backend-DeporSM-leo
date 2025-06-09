package com.example.deporsm.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotHorariosRequestDTO {
    private String instalacionNombre;
    private Long instalacionId;
    private String fecha;           // Para una fecha específica
    private String fechaInicio;     // Para rango de fechas
    private String fechaFin;        // Para rango de fechas
}
