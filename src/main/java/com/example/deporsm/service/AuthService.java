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
            String userType = getUserTypeInSpanish(usuario.getRol().getId());
            throw new RuntimeException("CUENTA_INACTIVA: Su cuenta de " + userType + " está inactiva. Contacte al administrador del sistema para reactivar su cuenta.");
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

    /**
     * Helper method to get user type in Spanish based on role ID
     * @param roleId The role ID
     * @return The user type in Spanish
     */
    private String getUserTypeInSpanish(Integer roleId) {
        switch (roleId) {
            case 1:
                return "superadministrador";
            case 2:
                return "administrador";
            case 3:
                return "coordinador";
            case 4:
                return "vecino";
            default:
                return "usuario";
        }
    }

    /**
     * Busca un usuario por su email retornando un Optional
     * @param email Email del usuario
     * @return Optional con el usuario si existe
     */
    public java.util.Optional<Usuario> findUserOptionalByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }
}
