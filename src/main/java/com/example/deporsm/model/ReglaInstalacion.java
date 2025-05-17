package com.example.deporsm.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Entity
@Table(name = "reglas_instalacion")
@Getter
@Setter
@NoArgsConstructor
public class ReglaInstalacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;    @ManyToOne
    @JoinColumn(name = "instalacion_id", nullable = false)
    @JsonIgnore
    private Instalacion instalacion;

    @Column(nullable = false)
    private String descripcion;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    public ReglaInstalacion(Instalacion instalacion, String descripcion) {
        this.instalacion = instalacion;
        this.descripcion = descripcion;
        this.createdAt = new Timestamp(System.currentTimeMillis());
        this.updatedAt = this.createdAt;
    }
}
