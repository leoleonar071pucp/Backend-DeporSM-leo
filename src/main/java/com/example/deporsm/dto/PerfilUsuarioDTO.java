package com.example.deporsm.dto;

/**
 * DTO para actualizar el perfil del usuario
 */
public class PerfilUsuarioDTO {
    private String telefono;
    private String direccion;

    // Constructores
    public PerfilUsuarioDTO() {
    }

    public PerfilUsuarioDTO(String telefono, String direccion) {
        this.telefono = telefono;
        this.direccion = direccion;
    }

    // Getters y setters
    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
}