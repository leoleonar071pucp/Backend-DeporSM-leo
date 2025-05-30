package com.example.deporsm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CrearReservaDTO {
    private Integer instalacionId;
    private String fecha;           // Cambiar a String para evitar problemas de zona horaria
    private String horaInicio;      // Cambiar a String para evitar problemas de zona horaria
    private String horaFin;         // Cambiar a String para evitar problemas de zona horaria
    private Integer numeroAsistentes;
    private String comentarios;
    private String estado;          // Estado de la reserva (pendiente, confirmada, etc)
    private String estadoPago;      // Estado del pago (pendiente, pagado, etc)
    private String metodoPago;      // Método de pago (online, deposito)
}
