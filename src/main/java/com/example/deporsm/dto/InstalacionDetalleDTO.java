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
public class InstalacionDetalleDTO {
    private Integer id;
    private String nombre;
    private String descripcion;
    private String ubicacion;
    private String tipo;
    private Integer capacidad;
    private String contactoNumero;
    private String imagenUrl;
    private float precio;
    private Boolean activo;
    private String estado; // "disponible", "mantenimiento", "ocupada"
    private String coordenadas; // Para mostrar en mapa (deprecated, usar latitud/longitud)
    private Double latitud;
    private Double longitud;
    private Integer radioValidacion;

    // Nuevos campos
    private List<String> caracteristicas;
    private List<String> comodidades;
    private List<String> reglas;
}
