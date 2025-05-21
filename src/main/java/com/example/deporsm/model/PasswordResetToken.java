package com.example.deporsm.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private Timestamp expiracion;

    @Column(nullable = false)
    private Boolean usado;

    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;

    public PasswordResetToken(Usuario usuario, String token) {
        this.usuario = usuario;
        this.token = token;
        this.usado = false;
        this.createdAt = Timestamp.valueOf(LocalDateTime.now());
        // Expiración por defecto: 24 horas desde la creación
        this.expiracion = Timestamp.valueOf(LocalDateTime.now().plusHours(24));
    }

    public boolean isExpirado() {
        return new Timestamp(System.currentTimeMillis()).after(expiracion);
    }

    public boolean isValido() {
        return !isExpirado() && !usado;
    }
}
