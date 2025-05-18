-- Script para crear la tabla bloqueos_temporales
-- Esta tabla almacena los bloqueos temporales de horarios durante el proceso de reserva

CREATE TABLE IF NOT EXISTS `bloqueos_temporales` (
  `id` int NOT NULL AUTO_INCREMENT,
  `instalacion_id` int NOT NULL,
  `usuario_id` int NOT NULL,
  `fecha` date NOT NULL,
  `hora_inicio` time NOT NULL,
  `hora_fin` time NOT NULL,
  `token` varchar(255) NOT NULL,
  `expiracion` timestamp NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_bloqueo_instalacion_idx` (`instalacion_id`),
  KEY `fk_bloqueo_usuario_idx` (`usuario_id`),
  CONSTRAINT `fk_bloqueo_instalacion` FOREIGN KEY (`instalacion_id`) REFERENCES `instalaciones` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_bloqueo_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
