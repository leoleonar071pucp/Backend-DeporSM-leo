// src/main/java/com/example/deporsm/dto/ObservacionInstalacionDTO.java
package com.example.deporsm.dto;

/**
 * DTO para representar observaciones recientes por instalación
 */
public interface ObservacionInstalacionDTO {
    Integer getIdObservacion();
    String getNombreInstalacion();
    String getTitulo();
    String getDescripcion();
    String getPrioridad();
    String getFecha();
    String getEstado();
    String getCoordinador();
    String getFotosUrl();
}
