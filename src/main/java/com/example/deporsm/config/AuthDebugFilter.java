package com.example.deporsm.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AuthDebugFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        System.out.println("\n🔍 FILTRO DE DEPURACIÓN DE AUTENTICACIÓN 🔍");
        System.out.println("URL: " + request.getMethod() + " " + request.getRequestURI());
        System.out.println("Auth presente: " + (auth != null));
        
        if (auth != null) {
            System.out.println("Usuario: " + auth.getName());
            System.out.println("Autenticado: " + auth.isAuthenticated());
            System.out.println("Principal: " + auth.getPrincipal());
            System.out.println("Autoridades/Roles: " + auth.getAuthorities());
            System.out.println("Detalles: " + auth.getDetails());
        }
        
        HttpSession session = request.getSession(false);
        System.out.println("Sesión: " + (session != null ? "Activa (ID: " + session.getId() + ")" : "No hay sesión"));
        System.out.println("🔍 FIN DEL FILTRO DE DEPURACIÓN 🔍\n");

        filterChain.doFilter(request, response);
    }
}