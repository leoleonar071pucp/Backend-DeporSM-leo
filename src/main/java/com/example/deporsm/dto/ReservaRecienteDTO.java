package com.example.deporsm.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public interface ReservaRecienteDTO {
    Integer getIdReserva();
    String getNombreUsuario();
    String getNombreInstalacion();
    LocalDate getFecha();
    LocalTime getHoraInicio();
    LocalTime getHoraFin();
    String getEstado();
    String getEstadoPago();
}
