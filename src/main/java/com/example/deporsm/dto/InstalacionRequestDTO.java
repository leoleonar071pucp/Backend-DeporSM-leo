package com.example.deporsm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InstalacionRequestDTO {
    private String nombre;
    private String descripcion;
    private String ubicacion;
    private String tipo;
    private Integer capacidad;
    private String contactoNumero;
    private String imagenUrl;
    private float precio;
    private Boolean activo;
    private Double latitud;
    private Double longitud;
    private Integer radioValidacion;

    // Listas para las características, comodidades y reglas
    private List<String> caracteristicas;
    private List<String> comodidades;
    private List<String> reglas;

    // Lista de horarios disponibles
    private List<HorarioDisponibleDTO> horariosDisponibles;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HorarioDisponibleDTO {
        private String diaSemana;
        private String horaInicio;
        private String horaFin;
    }
}
