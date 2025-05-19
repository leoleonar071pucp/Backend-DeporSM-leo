package com.example.deporsm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DTO para recibir solicitudes de creación de mantenimientos
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MantenimientoRequestDTO {
    private Integer instalacionId;
    private String motivo;
    private String tipo;
    private String descripcion;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private Boolean afectaDisponibilidad;
    private Integer registradoPorId;
}
