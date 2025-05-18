package com.example.deporsm.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioDTO {
    private Integer id;
    private String nombre;
    private String apellidos;
    private String email;
    private String telefono;
    private String dni;
    private Boolean activo;
    private Integer reservas;

    // Constructor
    public UsuarioDTO(Integer id, String nombre, String apellidos, String email, String telefono, String dni, Boolean activo, Integer reservas) {
        this.id = id;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.email = email;
        this.telefono = telefono;
        this.dni = dni;
        this.activo = activo;
        this.reservas = reservas;
    }
}
