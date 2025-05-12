package com.example.deporsm.service;

import com.example.deporsm.dto.CoordinadorDTO;
import com.example.deporsm.dto.PerfilUsuarioDTO;
import com.example.deporsm.model.Usuario;
import com.example.deporsm.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión de usuarios en la aplicación
 */
@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Obtiene todos los usuarios del sistema
     * @return Lista de usuarios
     */
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    /**
     * Obtiene todos los coordinadores
     * @return Lista de coordinadores con sus instalaciones asignadas
     */
    public List<CoordinadorDTO> listarCoordinadores() {
        return usuarioRepository.findAllCoordinadores();
    }

    /**
     * Busca un usuario por su email
     * @param email Email del usuario
     * @return Usuario encontrado o empty si no existe
     */
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    /**
     * Obtiene el perfil del usuario autenticado
     * @param authentication Autenticación del usuario
     * @return Usuario encontrado o empty si no existe
     */
    public Optional<Usuario> obtenerPerfilUsuario(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        
        String email = authentication.getName();
        return usuarioRepository.findByEmail(email);
    }

    /**
     * Actualiza el perfil del usuario
     * @param perfilDTO DTO con los datos a actualizar
     * @param authentication Autenticación del usuario
     * @return Usuario actualizado o empty si no existe
     */
    public Optional<Usuario> actualizarPerfil(PerfilUsuarioDTO perfilDTO, Authentication authentication) {
        Optional<Usuario> usuarioOpt = obtenerPerfilUsuario(authentication);
        
        if (usuarioOpt.isEmpty()) {
            return Optional.empty();
        }
        
        Usuario usuario = usuarioOpt.get();
        
        if (perfilDTO.getTelefono() != null) {
            usuario.setTelefono(perfilDTO.getTelefono());
        }
        
        if (perfilDTO.getDireccion() != null) {
            usuario.setDireccion(perfilDTO.getDireccion());
        }
        
        return Optional.of(usuarioRepository.save(usuario));
    }

    /**
     * Guarda un usuario en la base de datos
     * @param usuario Usuario a guardar
     * @return Usuario guardado
     */
    public Usuario guardarUsuario(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }
}