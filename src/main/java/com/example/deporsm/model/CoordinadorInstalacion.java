package com.example.deporsm.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Table(name = "coordinadores_instalaciones") // 🔧 nombre exacto según SQL y modelo ER
public class CoordinadorInstalacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false) // 🔧 obligatorio según SQL
    private Usuario usuario;    @ManyToOne
    @JoinColumn(name = "instalacion_id", nullable = false) // 🔧 obligatorio según SQL
    @JsonIgnore
    private Instalacion instalacion;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;
}
