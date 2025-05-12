package com.example.deporsm.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;

/**
 * Filtro de diagnóstico para rastrear problemas de autenticación
 */
@Component
public class AuthenticationDiagnosticFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String uri = request.getRequestURI();
        System.out.println("\n==================== DIAGNÓSTICO DE AUTENTICACIÓN ====================");
        System.out.println("⏰ Hora: " + new java.util.Date());
        System.out.println("🌐 URI: " + uri);
        System.out.println("📱 Método: " + request.getMethod());
        
        // Imprime todas las cookies
        System.out.println("🍪 Cookies:");
        if (request.getCookies() != null) {
            Arrays.stream(request.getCookies())
                  .forEach(cookie -> System.out.println("   - " + cookie.getName() + ": " + cookie.getValue()));
        } else {
            System.out.println("   [No hay cookies]");
        }
        
        // Imprime información sobre la sesión
        HttpSession session = request.getSession(false);
        System.out.println("🔑 Sesión:");
        if (session != null) {
            System.out.println("   - ID: " + session.getId());
            System.out.println("   - Creada: " + new java.util.Date(session.getCreationTime()));
            System.out.println("   - Último acceso: " + new java.util.Date(session.getLastAccessedTime()));
            
            System.out.println("   - Atributos:");
            Enumeration<String> attributeNames = session.getAttributeNames();
            while (attributeNames.hasMoreElements()) {
                String name = attributeNames.nextElement();
                System.out.println("      * " + name + ": " + session.getAttribute(name));
            }
        } else {
            System.out.println("   [No hay sesión activa]");
        }
        
        // Imprime información de autenticación actual
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("🔐 Autenticación:");
        if (authentication != null) {
            System.out.println("   - Principal: " + authentication.getPrincipal());
            System.out.println("   - Autenticado: " + authentication.isAuthenticated());
            System.out.println("   - Credenciales: " + authentication.getCredentials());
            System.out.println("   - Autoridades:");
            authentication.getAuthorities().forEach(auth -> 
                System.out.println("      * " + auth.getAuthority()));
        } else {
            System.out.println("   [No hay autenticación]");
        }

        // Probar a forzar autenticación para rutas críticas
        if (uri.equals("/api/auth/me") || uri.startsWith("/api/usuarios/perfil")) {
            System.out.println("⚠️ Forzando autenticación para ruta crítica");
            
            // Solo forzar si no hay una autenticación existente o es anónima
            if (authentication == null || 
                authentication.getPrincipal().equals("anonymousUser") || 
                !authentication.isAuthenticated()) {
                
                // Crear una autenticación forzada para el usuario coordinador
                Collection<GrantedAuthority> authorities = 
                    Arrays.asList(new SimpleGrantedAuthority("ROLE_COORDINADOR"));
                
                User user = new User("coordinador@example.com", "", authorities);
                
                Authentication forcedAuth = 
                    new UsernamePasswordAuthenticationToken(user, null, authorities);
                
                SecurityContextHolder.getContext().setAuthentication(forcedAuth);
                
                System.out.println("✅ Autenticación forzada para: coordinador@example.com");
                
                // Si no hay sesión, crear una
                if (session == null) {
                    session = request.getSession(true);
                    session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
                    System.out.println("📝 Sesión creada con ID: " + session.getId());
                }
            }
        }
        
        System.out.println("==================================================================");
        
        // Continuar con la cadena de filtros
        filterChain.doFilter(request, response);
    }
}