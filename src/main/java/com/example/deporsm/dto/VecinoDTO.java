package com.example.deporsm.dto;

public class VecinoDTO {
    private Integer id;
    private String nombre;
    private String apellidos;
    private String email;
    private String telefono;
    private String direccion;
    private String dni;
    private Boolean activo;
    private String lastLogin;
    private Long reservas;  // Número de reservas realizadas por el vecino
    
    public VecinoDTO(Integer id, String nombre, String apellidos, String email, String telefono, 
                     String direccion, String dni, Boolean activo, String lastLogin, Long reservas) {
        this.id = id;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.email = email;
        this.telefono = telefono;
        this.direccion = direccion;
        this.dni = dni;
        this.activo = activo;
        this.lastLogin = lastLogin;
        this.reservas = reservas;
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
    public String getApellidos() { return apellidos; }
    public String getEmail() { return email; }
    public String getTelefono() { return telefono; }
    public String getDireccion() { return direccion; }
    public String getDni() { return dni; }
    public Boolean getActivo() { return activo; }
    public String getLastLogin() { return lastLogin; }
    public Long getReservas() { return reservas; }

    public void setId(Integer id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public void setEmail(String email) { this.email = email; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public void setDni(String dni) { this.dni = dni; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public void setLastLogin(String lastLogin) { this.lastLogin = lastLogin; }
    public void setReservas(Long reservas) { this.reservas = reservas; }
}
