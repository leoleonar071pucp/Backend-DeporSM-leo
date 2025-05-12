package com.example.deporsm.controller;

import com.example.deporsm.model.LoginRequest;
import com.example.deporsm.model.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.Authentication;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public ResponseEntity<Usuario> login(@RequestBody LoginRequest request, HttpServletRequest servletRequest, HttpServletResponse response) {
        System.out.println("📥 Intentando login para: " + request.getEmail());

        try {
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());

            Authentication authentication = authenticationManager.authenticate(authToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Forzar la creación de una sesión si no existe
            HttpSession session = servletRequest.getSession(true);
            session.setMaxInactiveInterval(3600); // Sesión válida por 1 hora

            Usuario usuario = authService.findByEmail(request.getEmail());

            System.out.println("✅ Login exitoso para: " + request.getEmail());
            System.out.println("🍪 ID de sesión: " + session.getId());

            return ResponseEntity.ok(usuario);
        } catch (Exception e) {
            System.out.println("❌ Error en login: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/me")
    public ResponseEntity<Usuario> getCurrentUser(HttpServletRequest request) {
        System.out.println("🧠 Obteniendo usuario desde sesión...");

        try {
            // Intenta obtener la autenticación directamente del contexto de seguridad
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated() 
                || authentication.getPrincipal().equals("anonymousUser")) {
                System.out.println("🚫 Usuario no autenticado o anónimo");
                return ResponseEntity.status(401).build();
            }
            
            // Obtener el email del usuario autenticado
            String email = authentication.getName();
            System.out.println("📧 Email encontrado: " + email);
            
            // Obtener usuario de la base de datos
            Usuario usuario = authService.findByEmail(email);
            
            if (usuario == null) {
                System.out.println("❓ No se encontró usuario con email: " + email);
                return ResponseEntity.status(401).build();
            }
            
            System.out.println("✅ Usuario autenticado: " + usuario.getEmail() + ", rol: " + usuario.getRol().getNombre());
            return ResponseEntity.ok(usuario);
        } catch (Exception e) {
            System.out.println("❌ Error al obtener usuario: " + e.getMessage());
            e.printStackTrace(); // Imprime el stack trace para depuración
            return ResponseEntity.status(500).build();
        }
    }

    // Eliminamos el método logout para evitar conflictos con Spring Security
    // Spring Security manejará automáticamente las peticiones a /api/auth/logout
}