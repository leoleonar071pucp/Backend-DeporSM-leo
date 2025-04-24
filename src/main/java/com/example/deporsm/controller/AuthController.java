package com.example.deporsm.controller;

import com.example.deporsm.model.LoginRequest;
import com.example.deporsm.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
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
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true") // para cookies en Next.js
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public ResponseEntity<Usuario> login(@RequestBody LoginRequest request) {
        System.out.println("📥 Intentando login para: " + request.getEmail());

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());

        Authentication authentication = authenticationManager.authenticate(authToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Usuario usuario = authService.findByEmail(request.getEmail());

        return ResponseEntity.ok(usuario);
    }






    @GetMapping("/me")
    public ResponseEntity<Usuario> getCurrentUser(Authentication authentication) {
        System.out.println("🧠 Obteniendo usuario desde sesión...");

        if (authentication == null || !authentication.isAuthenticated()) {
            System.out.println("🚫 Usuario no autenticado");
            return ResponseEntity.status(401).build();
        }

        String email = authentication.getName();
        Usuario usuario = authService.findByEmail(email);

        return ResponseEntity.ok(usuario);
    }


}