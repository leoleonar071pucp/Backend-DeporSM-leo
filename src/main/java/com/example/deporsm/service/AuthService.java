package com.example.deporsm.service;

import com.example.deporsm.model.Usuario;
import com.example.deporsm.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public Usuario login(String email, String password) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!encoder.matches(password, usuario.getPassword())) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        if (!usuario.getActivo()) {
            throw new RuntimeException("Usuario inactivo");
        }

        return usuario;
    }
    
    /**
     * Busca un usuario por su email
     * @param email Email del usuario
     * @return Usuario encontrado
     * @throws RuntimeException si no existe el usuario
     */
    public Usuario findByEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    /**
     * Cambia la contraseña de un usuario
     * @param email Email del usuario
     * @param currentPassword Contraseña actual
     * @param newPassword Nueva contraseña
     * @return true si se cambió correctamente, false si la contraseña actual es incorrecta
     */
    public boolean changePassword(String email, String currentPassword, String newPassword) {
        Usuario usuario = findByEmail(email);
        
        // Verificar que la contraseña actual sea correcta
        if (!encoder.matches(currentPassword, usuario.getPassword())) {
            return false;
        }
        
        // Codificar la nueva contraseña
        String encodedPassword = encoder.encode(newPassword);
        
        // Actualizar la contraseña
        usuario.setPassword(encodedPassword);
        
        // Guardar el usuario actualizado en la base de datos
        usuarioRepository.save(usuario);
        
        return true;
    }

    public void logout() {
        // Lógica para logout si se requiere a nivel de servicio
    }
}
