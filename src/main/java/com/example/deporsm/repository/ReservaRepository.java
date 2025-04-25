package com.example.deporsm.repository;

import com.example.deporsm.model.Reserva;
import com.example.deporsm.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Integer> {

    // Buscar por entidad completa
    List<Reserva> findByUsuario(Usuario usuario);

    // O buscar por DNI del usuario
    List<Reserva> findByUsuario_Dni(String dni);
}
