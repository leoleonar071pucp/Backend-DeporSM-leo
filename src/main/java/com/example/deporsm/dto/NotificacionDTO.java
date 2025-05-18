package com.example.deporsm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionDTO {
    private Integer id;
    private String titulo;
    private String mensaje;
    private String tipo;
    private Boolean leida;
    private String fechaEnvio;
    private String categoria;
    private String feedback;
}
