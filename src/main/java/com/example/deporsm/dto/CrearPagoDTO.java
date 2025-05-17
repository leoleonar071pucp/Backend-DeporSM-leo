package com.example.deporsm.dto;

import java.math.BigDecimal;

public class CrearPagoDTO {
    private Integer reservaId;
    private BigDecimal monto;
    private String referenciaTransaccion;
    private String ultimosDigitos;
    private String metodo;
    
    public Integer getReservaId() {
        return reservaId;
    }
    
    public void setReservaId(Integer reservaId) {
        this.reservaId = reservaId;
    }
    
    public BigDecimal getMonto() {
        return monto;
    }
    
    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }
    
    public String getReferenciaTransaccion() {
        return referenciaTransaccion;
    }
    
    public void setReferenciaTransaccion(String referenciaTransaccion) {
        this.referenciaTransaccion = referenciaTransaccion;
    }
    
    public String getUltimosDigitos() {
        return ultimosDigitos;
    }
    
    public void setUltimosDigitos(String ultimosDigitos) {
        this.ultimosDigitos = ultimosDigitos;
    }
    
    public String getMetodo() {
        return metodo;
    }
    
    public void setMetodo(String metodo) {
        this.metodo = metodo;
    }
}
