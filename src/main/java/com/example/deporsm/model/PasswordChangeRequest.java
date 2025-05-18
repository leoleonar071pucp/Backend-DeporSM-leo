package com.example.deporsm.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Modelo para solicitud de cambio de contraseña
 */
@Getter
@Setter
public class PasswordChangeRequest {
    private String currentPassword;
    private String newPassword;
}
