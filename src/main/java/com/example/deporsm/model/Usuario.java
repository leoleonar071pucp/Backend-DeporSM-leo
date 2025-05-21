package com.example.deporsm.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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

    @CreationTimestamp
    @Column(name = "created_at")
    private Timestamp createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = new Timestamp(System.currentTimeMillis());
        }
        if (this.updatedAt == null) {
            this.updatedAt = new Timestamp(System.currentTimeMillis());
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }

    @OneToMany(mappedBy = "usuario")
    @JsonIgnore
    private List<Reserva> reservas;

    @OneToMany(mappedBy = "usuario")
    @JsonIgnore
    private List<Observacion> observaciones;

    @OneToMany(mappedBy = "usuario")
    @JsonIgnore
    private List<Notificacion> notificaciones;

    @OneToMany(mappedBy = "usuario")
    @JsonIgnore
    private List<Asistencia> asistencias;




    @OneToMany(mappedBy = "usuario")
    @JsonIgnore
    private List<CoordinadorInstalacion> coordinaciones;

    @OneToMany(mappedBy = "usuario")
    @JsonIgnore
    private List<LogActividad> logs;

    @OneToMany(mappedBy = "usuario")
    @JsonIgnore
    private List<PreferenciaNotificacion> preferencias;
}
