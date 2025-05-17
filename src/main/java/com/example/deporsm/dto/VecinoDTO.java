package com.example.deporsm.dto;

public class VecinoDTO {
    private Integer id;
    private String nombre;
    private String email;
    private String telefono;
    private Long reservas;  // Número de reservas realizadas por el vecino    // Constructor que debe coincidir EXACTAMENTE con el orden de la query
    
    public VecinoDTO(Integer id, String nombre, String email, String telefono, Long reservas) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.reservas = reservas;
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public String getTelefono() { return telefono; }
    public Long getReservas() { return reservas; }

    public void setId(Integer id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setEmail(String email) { this.email = email; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public void setReservas(Long reservas) { this.reservas = reservas; }
}
