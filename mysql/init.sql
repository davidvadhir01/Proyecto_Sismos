CREATE DATABASE IF NOT EXISTS sismos;
USE sismos;

-- Tabla de Usuarios
CREATE TABLE IF NOT EXISTS usuario (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  rol VARCHAR(50) NOT NULL DEFAULT 'USER'
);

-- Tabla de Administradores
CREATE TABLE IF NOT EXISTS administrador (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  rol VARCHAR(50) NOT NULL DEFAULT 'ADMIN',
  departamento VARCHAR(100),
  estado_activo BOOLEAN DEFAULT TRUE
);

-- Insertar un usuario administrador por defecto
INSERT INTO usuario (nombre, email, password, rol) 
VALUES ('Admin', 'admin@example.com', '$2a$10$uS1TVzZvGW1lS.LiYJ0tR.amVTaX0qrK9zfkA3OMuGlHVcmEm7L02', 'ADMIN');
-- La contraseña es "password" en BCrypt