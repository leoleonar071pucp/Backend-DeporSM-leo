-- Crear tabla para tokens de restablecimiento de contraseña
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id INT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    usuario_id INT NOT NULL,
    expiracion TIMESTAMP NOT NULL,
    usado BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

-- Crear índice para búsqueda rápida por token
CREATE INDEX idx_password_reset_token ON password_reset_tokens(token);

-- Crear índice para búsqueda por usuario
CREATE INDEX idx_password_reset_usuario ON password_reset_tokens(usuario_id);
