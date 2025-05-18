package com.example.deporsm.controller;

import com.example.deporsm.model.LoginRequest;
import com.example.deporsm.model.Usuario;
import com.example.deporsm.model.PasswordChangeRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.Authentication;
import java.util.Map;


@RestController
@RequestMapping("/api/auth")
@CrossOrigin(
  origins = {
    "https://deporsm-apiwith-1035693188565.us-central1.run.app",
    "https://frontend-depor-sm-leo.vercel.app",
    "http://localhost:3000"
  },
  allowCredentials = "true"
)
public class AuthController {
    
    @Autowired
    private com.example.deporsm.service.AuthService authService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public ResponseEntity<Usuario> login(@RequestBody LoginRequest request, HttpServletRequest servletRequest, HttpServletResponse response) {
        System.out.println("📥 Intentando login para: " + request.getEmail());

        try {
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());

            Authentication authentication = authenticationManager.authenticate(authToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);            // Forzar la creación de una sesión si no existe
            HttpSession session = servletRequest.getSession(true);
            session.setMaxInactiveInterval(86400); // Sesión válida por 24 horas (86400 segundos)
            
            // Guardar explícitamente el contexto de seguridad en la sesión para mejor persistencia
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

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

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody PasswordChangeRequest request) {
        System.out.println("📝 Intentando cambiar contraseña...");
        
        try {
            // Obtener el usuario autenticado actual
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated() 
                || authentication.getPrincipal().equals("anonymousUser")) {
                System.out.println("🚫 Usuario no autenticado o anónimo");
                return ResponseEntity.status(401).build();
            }
            
            String email = authentication.getName();
            System.out.println("📧 Cambiando contraseña para: " + email);
            
            // Intentar cambiar la contraseña
            boolean passwordChanged = authService.changePassword(
                email, 
                request.getCurrentPassword(),
                request.getNewPassword()
            );
            
            if (passwordChanged) {
                System.out.println("✅ Contraseña actualizada correctamente");
                return ResponseEntity.ok().body(Map.of("message", "Contraseña actualizada correctamente"));
            } else {
                System.out.println("❌ Error al actualizar contraseña: La contraseña actual no es válida");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "La contraseña actual no es válida"));
            }
        } catch (Exception e) {
            System.out.println("❌ Error al cambiar contraseña: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Error al cambiar la contraseña: " + e.getMessage()));
        }
    }

    // Eliminamos el método logout para evitar conflictos con Spring Security
    // Spring Security manejará automáticamente las peticiones a /api/auth/logout
}