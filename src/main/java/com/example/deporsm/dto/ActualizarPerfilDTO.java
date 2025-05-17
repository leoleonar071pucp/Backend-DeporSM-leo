package com.example.deporsm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarPerfilDTO {
    private String telefono;
    private String direccion;
    // No incluimos datos sensibles como contraseña, nombre o DNI
    // ya que estos requieren procesos especiales
}
