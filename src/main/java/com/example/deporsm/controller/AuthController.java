package com.example.deporsm.controller;

import com.example.deporsm.model.LoginRequest;
import com.example.deporsm.model.Usuario;
import com.example.deporsm.model.PasswordChangeRequest;
import com.example.deporsm.service.PasswordResetService;
import jakarta.mail.MessagingException;
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
    private PasswordResetService passwordResetService;

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

    /**
     * Solicita un restablecimiento de contraseña
     * @param request Mapa con el email del usuario
     * @return ResponseEntity con mensaje de éxito o error
     */
    @PostMapping("/request-password-reset")
    public ResponseEntity<?> requestPasswordReset(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        System.out.println("📧 Solicitando restablecimiento de contraseña para: " + email);

        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El correo electrónico es requerido"));
        }

        try {
            boolean emailSent = passwordResetService.solicitarRestablecimientoPassword(email);

            if (emailSent) {
                System.out.println("✅ Correo de restablecimiento enviado a: " + email);
                return ResponseEntity.ok(Map.of("message", "Se ha enviado un correo con instrucciones para restablecer tu contraseña"));
            } else {
                // No informamos al usuario si el correo no existe por seguridad
                System.out.println("ℹ️ Correo no encontrado, pero no informamos al usuario: " + email);
                return ResponseEntity.ok(Map.of("message", "Si el correo existe en nuestra base de datos, recibirás instrucciones para restablecer tu contraseña"));
            }
        } catch (MessagingException e) {
            System.out.println("❌ Error al enviar correo: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Error al enviar el correo de restablecimiento. Verifica la configuración de correo."));
        } catch (Exception e) {
            System.out.println("❌ Error inesperado: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Error inesperado al procesar la solicitud: " + e.getMessage()));
        }
    }

    /**
     * Valida un token de restablecimiento de contraseña
     * @param token Token a validar
     * @return ResponseEntity con resultado de la validación
     */
    @GetMapping("/validate-reset-token/{token}")
    public ResponseEntity<?> validateResetToken(@PathVariable String token) {
        System.out.println("🔍 Validando token de restablecimiento: " + token);

        boolean isValid = passwordResetService.validarToken(token);

        if (isValid) {
            System.out.println("✅ Token válido: " + token);
            return ResponseEntity.ok(Map.of("valid", true));
        } else {
            System.out.println("❌ Token inválido o expirado: " + token);
            return ResponseEntity.ok(Map.of("valid", false));
        }
    }

    /**
     * Restablece la contraseña usando un token
     * @param request Mapa con el token y la nueva contraseña
     * @return ResponseEntity con mensaje de éxito o error
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");
        String confirmPassword = request.get("confirmPassword");

        System.out.println("🔄 Restableciendo contraseña con token: " + token);

        if (token == null || token.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El token es requerido"));
        }

        if (newPassword == null || newPassword.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "La nueva contraseña es requerida"));
        }

        if (!newPassword.equals(confirmPassword)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Las contraseñas no coinciden"));
        }

        try {
            boolean passwordReset = passwordResetService.restablecerPassword(token, newPassword);

            if (passwordReset) {
                System.out.println("✅ Contraseña restablecida exitosamente");
                return ResponseEntity.ok(Map.of("message", "Contraseña restablecida exitosamente"));
            } else {
                System.out.println("❌ Token inválido o expirado");
                return ResponseEntity.badRequest().body(Map.of("error", "El enlace de restablecimiento es inválido o ha expirado"));
            }
        } catch (Exception e) {
            System.out.println("❌ Error al restablecer contraseña: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Error al restablecer la contraseña: " + e.getMessage()));
        }
    }
}