package com.example.deporsm.controller;

// This file redirects functionality to com.example.deporsm.service.AuthService
// Keeping this file to prevent any potential import issues from other parts of the codebase
// In the future, direct imports to service.AuthService should be used instead

import com.example.deporsm.model.Usuario;
import com.example.deporsm.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This class is a wrapper around the AuthService in the service package.
 * It exists only for backward compatibility and to prevent import errors.
 * All new code should use com.example.deporsm.service.AuthService directly.
 */
@Component
public class AuthServiceWrapper {

    @Autowired
    private AuthService serviceAuthService;

    /**
     * Delegates login functionality to the service package's AuthService
     */
    public Usuario login(String email, String password) {
        return serviceAuthService.login(email, password);
    }

    /**
     * Delegates findByEmail functionality to the service package's AuthService
     */
    public Usuario findByEmail(String email) {
        return serviceAuthService.findByEmail(email);
    }
    
    /**
     * Delegates logout functionality to the service package's AuthService
     */
    public void logout() {
        serviceAuthService.logout();
    }
}
