package com.example.deporsm.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nombre;
    private String apellidos;
    private String email;
    private String password;
    private String telefono;
    private String direccion;
    private String dni;

    @ManyToOne
    @JoinColumn(name = "role_id") // Relación con tabla roles
    private Rol rol;

    @Column(name = "avatar_url")
    private String avatarUrl;

    private Boolean activo;

    @Column(name = "last_login")
    private Timestamp lastLogin;

    @Column(name = "ip_address")
    private String ipAddress;

    private String device;

    @Column(name = "fecha_expiracion_password")
    private Date fechaExpiracionPassword;

    @Column(name = "intentos_fallidos")
    private Integer intentosFallidos;

    @Column(name = "bloqueado_hasta")
    private Timestamp bloqueadoHasta;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @OneToMany(mappedBy = "usuario")
    private List<Reserva> reservas;

    @OneToMany(mappedBy = "usuario")
    private List<Observacion> observaciones;

    @OneToMany(mappedBy = "usuario")
    private List<Notificacion> notificaciones;

    @OneToMany(mappedBy = "usuario")
    private List<Asistencia> asistencias;




    @OneToMany(mappedBy = "usuario")
    private List<CoordinadorInstalacion> coordinaciones;

    @OneToMany(mappedBy = "usuario")
    private List<LogActividad> logs;

    @OneToMany(mappedBy = "usuario")
    private List<PreferenciaNotificacion> preferencias;
}
