package com.example.deporsm.dto;

import java.time.LocalTime;

public interface ReservaRecienteDTO {
    Integer getIdReserva();
    String getNombreUsuario();
    String getNombreInstalacion();
    Integer getInstalacionId();
    String getFecha();  // Cambiado a String para recibir la fecha formateada desde SQL
    LocalTime getHoraInicio();
    LocalTime getHoraFin();
    String getEstado();
    String getEstadoPago();
}
