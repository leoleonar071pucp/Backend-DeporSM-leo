package com.example.deporsm.controller;

import com.example.deporsm.dto.ActualizarPerfilDTO;
import com.example.deporsm.dto.CambioPasswordDTO;
import com.example.deporsm.dto.CoordinadorDTO;
import com.example.deporsm.dto.PerfilUsuarioDTO;
import com.example.deporsm.dto.PreferenciasNotificacionDTO;
import com.example.deporsm.model.Usuario;
import com.example.deporsm.repository.UsuarioRepository;
import com.example.deporsm.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public List<Usuario> listarUsuarios() {
        return usuarioService.listarUsuarios();
    }

    @PostMapping
    public Usuario crearUsuario(@RequestBody Usuario usuario) {
        return usuarioService.guardarUsuario(usuario);
    }

    @GetMapping("/allCoordinadores")
    public List<CoordinadorDTO> listarCoordinadores() {
        return usuarioService.listarCoordinadores();
    }
    
    @GetMapping("/perfil")
    public ResponseEntity<Usuario> getPerfil() {
        System.out.println("[DEBUG] Iniciando getPerfil");
        try {
            // Obtener autenticación directamente del contexto de seguridad
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated() 
                || authentication.getPrincipal().equals("anonymousUser")) {
                System.out.println("[DEBUG] getPerfil - Usuario no autenticado o anónimo");
                return ResponseEntity.status(401).build();
            }
            
            String email = authentication.getName();
            System.out.println("[DEBUG] getPerfil - Email encontrado: " + email);
            
            Optional<Usuario> usuario = usuarioRepository.findByEmail(email);
            
            if (usuario.isPresent()) {
                System.out.println("[DEBUG] getPerfil - Usuario encontrado: " + usuario.get().getEmail() 
                    + ", rol: " + usuario.get().getRol().getNombre());
                return ResponseEntity.ok(usuario.get());
            } else {
                System.out.println("[DEBUG] getPerfil - No se encontró usuario con email: " + email);
                return ResponseEntity.status(401).build();
            }
        } catch (Exception e) {
            System.out.println("[DEBUG] Error en getPerfil: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
    
    @PutMapping("/perfil")
    public ResponseEntity<Usuario> actualizarPerfil(@RequestBody PerfilUsuarioDTO perfilDTO) {
        System.out.println("[DEBUG] Iniciando actualizarPerfil");
        try {
            // Obtener autenticación directamente del contexto de seguridad
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated() 
                || authentication.getPrincipal().equals("anonymousUser")) {
                System.out.println("[DEBUG] actualizarPerfil - Usuario no autenticado o anónimo");
                return ResponseEntity.status(401).build();
            }
            
            String email = authentication.getName();
            System.out.println("[DEBUG] actualizarPerfil - Email encontrado: " + email);
            
            Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
            
            if (usuarioOpt.isEmpty()) {
                System.out.println("[DEBUG] actualizarPerfil - No se encontró usuario con email: " + email);
                return ResponseEntity.status(401).build();
            }
            
            Usuario usuario = usuarioOpt.get();
            
            if (perfilDTO.getTelefono() != null) {
                usuario.setTelefono(perfilDTO.getTelefono());
            }
            
            if (perfilDTO.getDireccion() != null) {
                usuario.setDireccion(perfilDTO.getDireccion());
            }
            
            usuario = usuarioRepository.save(usuario);
            
            System.out.println("[DEBUG] actualizarPerfil - Usuario actualizado: " + usuario.getEmail());
            return ResponseEntity.ok(usuario);
        } catch (Exception e) {
            System.out.println("[DEBUG] Error en actualizarPerfil: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @PutMapping("/actualizar-perfil")
    public ResponseEntity<?> actualizarPerfil(@RequestBody ActualizarPerfilDTO perfilDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() 
            || authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }
        
        String email = authentication.getName();
        try {
            usuarioService.actualizarPerfil(email, perfilDTO);
            return ResponseEntity.ok().body("Perfil actualizado correctamente");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al actualizar el perfil: " + e.getMessage());
        }
    }
    
    @PutMapping("/cambiar-password")
    public ResponseEntity<?> cambiarPassword(@RequestBody CambioPasswordDTO cambioPasswordDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() 
            || authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }
        
        String email = authentication.getName();
        try {
            if (!cambioPasswordDTO.getPasswordNueva().equals(cambioPasswordDTO.getConfirmacionPassword())) {
                return ResponseEntity.badRequest().body("Las contraseñas no coinciden");
            }
            
            usuarioService.cambiarPassword(email, cambioPasswordDTO);
            return ResponseEntity.ok().body("Contraseña actualizada correctamente");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al cambiar la contraseña: " + e.getMessage());
        }
    }
    
    @PutMapping("/preferencias-notificaciones")
    public ResponseEntity<?> actualizarPreferenciasNotificaciones(
            @RequestBody PreferenciasNotificacionDTO preferenciasDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() 
            || authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }
        
        String email = authentication.getName();
        try {
            usuarioService.actualizarPreferenciasNotificaciones(email, preferenciasDTO);
            return ResponseEntity.ok().body("Preferencias de notificaciones actualizadas correctamente");
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Error al actualizar las preferencias de notificaciones: " + e.getMessage());
        }
    }
}

