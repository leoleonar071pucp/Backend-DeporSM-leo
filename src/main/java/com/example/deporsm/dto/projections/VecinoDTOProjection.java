package com.example.deporsm.dto.projections;

public interface VecinoDTOProjection {
    Integer getId();
    String getNombre();
    String getApellidos();
    String getEmail();
    String getTelefono();
    String getDireccion();
    String getDni();
    Boolean getActivo();
    String getLastLogin();
    Long getReservas();
}
