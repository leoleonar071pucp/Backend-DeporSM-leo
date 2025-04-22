package com.example.deporsm.repository;

import com.example.deporsm.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, String> {

    // Este método busca un usuario por su correo
    Usuario findByCorreo(String correo);
}


