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

        // Verificar si el usuario está inactivo y lanzar excepción personalizada
        if (!usuario.getActivo()) {
            String userType = getUserTypeInSpanish(usuario.getRol().getId());
            throw new UsernameNotFoundException("CUENTA_INACTIVA: Su cuenta de " + userType + " está inactiva. Contacte al administrador del sistema para reactivar su cuenta.");
        }

        // Verificar si el rol existe y obtener el nombre
        String roleName = (usuario.getRol() != null) ? usuario.getRol().getNombre().toUpperCase() : "USER";

        return User
                .withUsername(usuario.getEmail())
                .password(usuario.getPassword()) // ⚠️ debe estar encriptada (BCrypt)
                .roles(roleName) // fuerza mayúsculas para compatibilidad Spring Security
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false) // Siempre habilitado ya que verificamos activo arriba
                .build();
    }

    private String getUserTypeInSpanish(Integer rolId) {
        switch (rolId) {
            case 1: return "vecino";
            case 2: return "coordinador";
            case 3: return "administrador";
            case 4: return "super administrador";
            default: return "usuario";
        }
    }
}
