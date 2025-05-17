package com.example.deporsm.service;

import com.example.deporsm.model.Usuario;
import com.example.deporsm.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));

        // Verificar si el rol existe y obtener el nombre
        String roleName = (usuario.getRol() != null) ? usuario.getRol().getNombre().toUpperCase() : "USER";

        return User
                .withUsername(usuario.getEmail())
                .password(usuario.getPassword()) // ⚠️ debe estar encriptada (BCrypt)
                .roles(roleName) // fuerza mayúsculas para compatibilidad Spring Security
                .disabled(!usuario.getActivo()) // Cuenta deshabilitada si usuario no está activo
                .build();
    }
}
