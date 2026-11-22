-- =============================================================================
-- KODEX - Script de Creación y Poblado de Base de Datos MySQL
-- Proyecto: Sistema Integral de Gestión de Biblioteca con Préstamos
-- Asignatura: Programación en Java - UTS
-- =============================================================================

CREATE DATABASE IF NOT EXISTS `kodex_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `kodex_db`;

-- Tabla de Configuraciones Generales
CREATE TABLE IF NOT EXISTS `configuracion` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `dias_prestamo` INT NOT NULL DEFAULT 7,
    `max_prestamos_activos` INT NOT NULL DEFAULT 3,
    `valor_multa_diaria` DECIMAL(10,2) NOT NULL DEFAULT 1000.00,
    `dias_vencimiento_reserva` INT NOT NULL DEFAULT 5,
    `nombre_biblioteca` VARCHAR(150) NOT NULL DEFAULT 'Biblioteca KODEX'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla de Categorías de Libros
CREATE TABLE IF NOT EXISTS `categoria` (
    `id_categoria` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `nombre` VARCHAR(100) NOT NULL UNIQUE,
    `descripcion` VARCHAR(255) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla de Lectores
CREATE TABLE IF NOT EXISTS `lector` (
    `id_lector` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `documento` VARCHAR(30) NOT NULL UNIQUE,
    `nombre` VARCHAR(150) NOT NULL,
    `telefono` VARCHAR(30) NULL,
    `correo` VARCHAR(150) NOT NULL,
    `estado` ENUM('ACTIVO', 'SANCIONADO') NOT NULL DEFAULT 'ACTIVO',
    `fecha_registro` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla de Usuarios del Sistema (6 roles)
CREATE TABLE IF NOT EXISTS `usuario` (
    `id_usuario` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `nombre` VARCHAR(150) NOT NULL,
    `correo` VARCHAR(150) NOT NULL UNIQUE,
    `clave_hash` VARCHAR(255) NOT NULL,
    `rol` ENUM('ADMIN', 'BIBLIOTECARIO', 'CATALOGADOR', 'TESORERO', 'GERENTE', 'LECTOR') NOT NULL,
    `activo` BOOLEAN NOT NULL DEFAULT TRUE,
    `id_lector` BIGINT NULL UNIQUE,
    CONSTRAINT `fk_usuario_lector` FOREIGN KEY (`id_lector`) REFERENCES `lector` (`id_lector`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla de Libros
CREATE TABLE IF NOT EXISTS `libro` (
    `id_libro` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `isbn` VARCHAR(30) NOT NULL UNIQUE,
    `titulo` VARCHAR(200) NOT NULL,
    `autor` VARCHAR(150) NOT NULL,
    `editorial` VARCHAR(100) NOT NULL,
    `anio` INT NOT NULL,
    `id_categoria` BIGINT NOT NULL,
    CONSTRAINT `fk_libro_categoria` FOREIGN KEY (`id_categoria`) REFERENCES `categoria` (`id_categoria`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla de Ejemplares Físicos
CREATE TABLE IF NOT EXISTS `ejemplar` (
    `id_ejemplar` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `codigo_inventario` VARCHAR(50) NOT NULL UNIQUE,
    `estado` ENUM('DISPONIBLE', 'PRESTADO', 'RESERVADO', 'EN_REPARACION', 'BAJA') NOT NULL DEFAULT 'DISPONIBLE',
    `ubicacion` VARCHAR(100) NOT NULL,
    `id_libro` BIGINT NOT NULL,
    CONSTRAINT `fk_ejemplar_libro` FOREIGN KEY (`id_libro`) REFERENCES `libro` (`id_libro`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla de Préstamos
CREATE TABLE IF NOT EXISTS `prestamo` (
    `id_prestamo` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `fecha_prestamo` DATE NOT NULL,
    `fecha_limite` DATE NOT NULL,
    `fecha_devolucion` DATE NULL,
    `renovaciones` INT NOT NULL DEFAULT 0,
    `estado` ENUM('ACTIVO', 'DEVUELTO', 'VENCIDO') NOT NULL DEFAULT 'ACTIVO',
    `id_lector` BIGINT NOT NULL,
    `id_ejemplar` BIGINT NOT NULL,
    `id_usuario` BIGINT NOT NULL,
    CONSTRAINT `fk_prestamo_lector` FOREIGN KEY (`id_lector`) REFERENCES `lector` (`id_lector`),
    CONSTRAINT `fk_prestamo_ejemplar` FOREIGN KEY (`id_ejemplar`) REFERENCES `ejemplar` (`id_ejemplar`),
    CONSTRAINT `fk_prestamo_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla de Multas
CREATE TABLE IF NOT EXISTS `multa` (
    `id_multa` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `dias_retraso` INT NOT NULL DEFAULT 0,
    `valor` DECIMAL(10,2) NOT NULL,
    `fecha_pago` DATE NULL,
    `estado` ENUM('PENDIENTE', 'PAGADA') NOT NULL DEFAULT 'PENDIENTE',
    `id_prestamo` BIGINT NOT NULL UNIQUE,
    CONSTRAINT `fk_multa_prestamo` FOREIGN KEY (`id_prestamo`) REFERENCES `prestamo` (`id_prestamo`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla de Reservas de Libros
CREATE TABLE IF NOT EXISTS `reserva_libro` (
    `id_reserva` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `fecha_solicitud` DATE NOT NULL,
    `fecha_vencimiento` DATE NOT NULL,
    `estado` ENUM('ACTIVA', 'ATENDIDA', 'CANCELADA', 'EXPIRADA') NOT NULL DEFAULT 'ACTIVA',
    `id_lector` BIGINT NOT NULL,
    `id_libro` BIGINT NOT NULL,
    CONSTRAINT `fk_reserva_lector` FOREIGN KEY (`id_lector`) REFERENCES `lector` (`id_lector`),
    CONSTRAINT `fk_reserva_libro` FOREIGN KEY (`id_libro`) REFERENCES `libro` (`id_libro`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- Datos Iniciales / Semilla (Pass: kodex123 cifrada con BCrypt: $2a$10$wK1F5N8K72O7tq6a9V/n6.B3H.o5eSvy6W3cK33nE9q6E4F8V.5gC)
-- BCrypt para 'kodex123': $2a$10$e8R..jMvh2hYwM1tQ7mRnu0B4bK.v3/yq3w9u1E6v.2V3m6/kF7Q6
-- -----------------------------------------------------------------------------
INSERT INTO `configuracion` (`id`, `dias_prestamo`, `max_prestamos_activos`, `valor_multa_diaria`, `dias_vencimiento_reserva`, `nombre_biblioteca`)
VALUES (1, 7, 3, 1000.00, 5, 'KODEX - Sistema Bibliotecario')
ON DUPLICATE KEY UPDATE `id` = `id`;

-- Categorías
INSERT INTO `categoria` (`id_categoria`, `nombre`, `descripcion`) VALUES
(1, 'Ingeniería y Tecnología', 'Libros de desarrollo, arquitectura de software, bases de datos y algoritmos'),
(2, 'Ciencias Básicas', 'Matemáticas, física, estadística y métodos formales'),
(3, 'Literatura y Humanidades', 'Novelas, narrativa clásica, historia y ensayos'),
(4, 'Administración y Negocios', 'Gestión de proyectos, liderazgo y economía')
ON DUPLICATE KEY UPDATE `id_categoria` = `id_categoria`;

-- Lectores
INSERT INTO `lector` (`id_lector`, `documento`, `nombre`, `telefono`, `correo`, `estado`) VALUES
(1, '1098765432', 'Carlos Andrés Pérez', '3104567890', 'lector@kodex.com', 'ACTIVO'),
(2, '1098765433', 'María Fernanda Gómez', '3117891234', 'maria.gomez@uts.edu.co', 'ACTIVO'),
(3, '1098765434', 'Juan David Rodríguez', '3123456789', 'juan.rodriguez@uts.edu.co', 'ACTIVO')
ON DUPLICATE KEY UPDATE `id_lector` = `id_lector`;

-- Usuarios de Prueba con BCrypt('kodex123')
-- Hash real compatible con Spring Security BCrypt: $2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW
INSERT INTO `usuario` (`id_usuario`, `nombre`, `correo`, `clave_hash`, `rol`, `activo`, `id_lector`) VALUES
(1, 'Jan Carlos Galvis (Admin)', 'admin@kodex.com', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'ADMIN', TRUE, NULL),
(2, 'Anphernee Hooker (Bibliotecario)', 'bibliotecario@kodex.com', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'BIBLIOTECARIO', TRUE, NULL),
(3, 'Paola Velasco (Catalogador)', 'catalogador@kodex.com', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'CATALOGADOR', TRUE, NULL),
(4, 'Tesorero General KODEX', 'tesorero@kodex.com', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'TESORERO', TRUE, NULL),
(5, 'Gerencia Académica UTS', 'gerente@kodex.com', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'GERENTE', TRUE, NULL),
(6, 'Carlos Andrés Pérez (Lector)', 'lector@kodex.com', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'LECTOR', TRUE, 1)
ON DUPLICATE KEY UPDATE `id_usuario` = `id_usuario`;

-- Libros
INSERT INTO `libro` (`id_libro`, `isbn`, `titulo`, `autor`, `editorial`, `anio`, `id_categoria`) VALUES
(1, '978-0134685991', 'Effective Java (3rd Edition)', 'Joshua Bloch', 'Addison-Wesley', 2018, 1),
(2, '978-0132350884', 'Clean Code', 'Robert C. Martin', 'Prentice Hall', 2008, 1),
(3, '978-0201633610', 'Design Patterns (GoF)', 'Erich Gamma et al.', 'Addison-Wesley', 1994, 1),
(4, '978-0321125217', 'Domain-Driven Design', 'Eric Evans', 'Addison-Wesley', 2003, 1),
(5, '978-0307474728', 'Cien Años de Soledad', 'Gabriel García Márquez', 'Sudamericana', 1967, 3)
ON DUPLICATE KEY UPDATE `id_libro` = `id_libro`;

-- Ejemplares Físicos
INSERT INTO `ejemplar` (`id_ejemplar`, `codigo_inventario`, `estado`, `ubicacion`, `id_libro`) VALUES
(1, 'KDX-LIB-001-A', 'DISPONIBLE', 'Estante A-1, Pasillo 1', 1),
(2, 'KDX-LIB-001-B', 'DISPONIBLE', 'Estante A-1, Pasillo 1', 1),
(3, 'KDX-LIB-002-A', 'DISPONIBLE', 'Estante A-2, Pasillo 1', 2),
(4, 'KDX-LIB-002-B', 'DISPONIBLE', 'Estante A-2, Pasillo 1', 2),
(5, 'KDX-LIB-003-A', 'DISPONIBLE', 'Estante B-1, Pasillo 2', 3),
(6, 'KDX-LIB-004-A', 'DISPONIBLE', 'Estante B-2, Pasillo 2', 4),
(7, 'KDX-LIB-005-A', 'DISPONIBLE', 'Estante C-1, Pasillo 3', 5)
ON DUPLICATE KEY UPDATE `id_ejemplar` = `id_ejemplar`;
