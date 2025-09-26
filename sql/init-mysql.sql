-- MediLink - Esquema MySQL minimo
-- Base de datos
CREATE DATABASE IF NOT EXISTS hospital CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE hospital;

-- Limpiar tablas si existen (orden por dependencias)
SET FOREIGN_KEY_CHECKS=0;
DROP TABLE IF EXISTS diagnostico;
DROP TABLE IF EXISTS usuario;
DROP TABLE IF EXISTS estado_entidad;
DROP TABLE IF EXISTS paciente;
DROP TABLE IF EXISTS medico;
SET FOREIGN_KEY_CHECKS=1;

-- Tabla de medicos
CREATE TABLE medico (
  id_medico INT AUTO_INCREMENT PRIMARY KEY,
  nombres VARCHAR(100) NOT NULL,
  apellidos VARCHAR(100) NOT NULL,
  especialidad VARCHAR(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabla de pacientes
CREATE TABLE paciente (
  id_paciente INT AUTO_INCREMENT PRIMARY KEY,
  nombres VARCHAR(100) NOT NULL,
  apellidos VARCHAR(100) NOT NULL,
  edad INT NOT NULL,
  genero VARCHAR(20) NULL,
  correo VARCHAR(150) NULL,
  direccion VARCHAR(200) NOT NULL,
  tipo_documento VARCHAR(50) NOT NULL,
  nro_documento VARCHAR(50) NOT NULL,
  nro_contacto VARCHAR(50) NULL,
  KEY idx_paciente_doc (nro_documento)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabla de usuarios (vinculados a medicos)
CREATE TABLE usuario (
  id_usuario INT AUTO_INCREMENT PRIMARY KEY,
  medico_id_medico INT NOT NULL,
  nombre VARCHAR(100) NOT NULL,
  contrasena VARCHAR(100) NOT NULL,
  CONSTRAINT fk_usuario_medico FOREIGN KEY (medico_id_medico) REFERENCES medico(id_medico)
    ON DELETE CASCADE ON UPDATE CASCADE,
  KEY idx_usuario_medico (medico_id_medico)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabla de diagnosticos
CREATE TABLE diagnostico (
  id_diagnostico INT AUTO_INCREMENT PRIMARY KEY,
  medico_id_medico INT NOT NULL,
  paciente_id_paciente INT NOT NULL,
  observaciones TEXT NOT NULL,
  fecha DATE NOT NULL,
  hora TIME NOT NULL,
  CONSTRAINT fk_diag_medico FOREIGN KEY (medico_id_medico) REFERENCES medico(id_medico)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_diag_paciente FOREIGN KEY (paciente_id_paciente) REFERENCES paciente(id_paciente)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  KEY idx_diag_medico (medico_id_medico),
  KEY idx_diag_paciente (paciente_id_paciente)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabla de estado_entidad (para toggles de activo)
CREATE TABLE estado_entidad (
  tipo_entidad VARCHAR(50) NOT NULL, -- 'paciente' o 'medico'
  id_entidad INT NOT NULL,
  activo TINYINT(1) NOT NULL DEFAULT 1,
  fecha_cambio TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uk_estado UNIQUE (tipo_entidad, id_entidad)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Datos de muestra minimos
INSERT INTO medico (nombres, apellidos, especialidad) VALUES
('Ana', 'Gomez', 'Cardiologia'),
('Luis', 'Perez', 'Pediatria');

INSERT INTO paciente (nombres, apellidos, edad, genero, correo, direccion, tipo_documento, nro_documento, nro_contacto) VALUES
('Maria', 'Lopez', 30, 'F', 'maria@example.com', 'Calle 123', 'DNI', '12345678', '555-1111'),
('Carlos', 'Diaz', 45, 'M', 'carlos@example.com', 'Av. 456', 'DNI', '87654321', '555-2222');

-- Usuario auto si deseas probar
INSERT INTO usuario (medico_id_medico, nombre, contrasena) VALUES (1, 'ana.gomez1', 'ag01');
