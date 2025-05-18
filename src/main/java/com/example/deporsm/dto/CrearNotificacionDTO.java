package com.example.deporsm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CrearNotificacionDTO {
    private String titulo;
    private String mensaje;
    private String tipo;
    private String categoria;
    private String feedback;
}
