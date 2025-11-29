DROP DATABASE IF EXISTS biblioteca_don_bosco;
CREATE DATABASE IF NOT EXISTS biblioteca_don_bosco;
USE biblioteca_don_bosco;

-- Tabla de Roles
CREATE TABLE Roles (
    id_rol INT PRIMARY KEY AUTO_INCREMENT,
    nombre_rol VARCHAR(50) NOT NULL UNIQUE,
    cant_max_prestamo INT NOT NULL,
    dias_prestamo INT NOT NULL,
    mora_diaria DECIMAL(10,2) NOT NULL
);

-- Tabla de Usuarios
CREATE TABLE Usuarios (
    id_usuario INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    correo VARCHAR(100) UNIQUE,
    contrasena VARCHAR(255) NOT NULL, -- Encriptada
    id_rol INT NOT NULL,
    FOREIGN KEY (id_rol) REFERENCES Roles(id_rol)
);

-- Catálogo de Autores
CREATE TABLE Autores (
    id_autor INT PRIMARY KEY AUTO_INCREMENT,
    nombre_autor VARCHAR(200) NOT NULL UNIQUE -- Asumiendo nombre único para simplificar
);

-- Catálogo de Editoriales
CREATE TABLE Editoriales (
    id_editorial INT PRIMARY KEY AUTO_INCREMENT,
    nombre_editorial VARCHAR(100) NOT NULL UNIQUE -- Asumiendo nombre único para simplificar
);

-- Catálogo de Géneros
CREATE TABLE Generos (
    id_genero INT PRIMARY KEY AUTO_INCREMENT,
    nombre_genero VARCHAR(100) NOT NULL UNIQUE -- Asumiendo nombre único para simplificar
);

-- Catálogo de Tipos de Documento Detalle (para Documentos, Periódicos, Revistas)
CREATE TABLE TiposDocumentoDetalle (
    id_tipo_detalle INT PRIMARY KEY AUTO_INCREMENT,
    nombre_tipo_detalle VARCHAR(100) NOT NULL UNIQUE -- informe, memorando, etc.
);

-- Catálogo de Tipos de Periódico
CREATE TABLE TiposPeriodico (
    id_tipo_periodico INT PRIMARY KEY AUTO_INCREMENT,
    nombre_tipo_periodico VARCHAR(100) NOT NULL UNIQUE -- local, nacional, etc.
);

-- Catálogo de Tipos de Revista
CREATE TABLE TiposRevista (
    id_tipo_revista INT PRIMARY KEY AUTO_INCREMENT,
    nombre_tipo_revista VARCHAR(100) NOT NULL UNIQUE -- científica, cultural, etc.
);

-- Catálogo de Tipos de Cinta
CREATE TABLE TiposCinta (
    id_tipo_cinta INT PRIMARY KEY AUTO_INCREMENT,
    nombre_tipo_cinta VARCHAR(50) NOT NULL UNIQUE -- audio, video
);

-- Tabla de Ejemplares (general - representa el título)
CREATE TABLE Ejemplares (
    id_ejemplar INT PRIMARY KEY AUTO_INCREMENT,
    titulo VARCHAR(200) NOT NULL,
    id_autor INT, -- Referencia al catálogo de Autores
    ubicacion VARCHAR(100),
    tipo_documento ENUM('Libro', 'Diccionario', 'Mapas', 'Tesis', 'DVD', 'VHS', 'Cassettes', 'CD', 'Documento', 'Periodicos', 'Revistas') NOT NULL,
    FOREIGN KEY (id_autor) REFERENCES Autores(id_autor)
    -- No se incluye estado aquí, ya que es por copia
);

-- Tabla de Copias (cada copia física individual de un ejemplar)
CREATE TABLE Copias (
    id_copia INT PRIMARY KEY AUTO_INCREMENT,
    id_ejemplar INT NOT NULL,
    codigo_unico VARCHAR(50) UNIQUE NOT NULL, -- Código único de la copia física
    estado ENUM('Disponible', 'Prestado', 'Reservado', 'No Disponible') DEFAULT 'Disponible', -- Estado individual de la copia
    FOREIGN KEY (id_ejemplar) REFERENCES Ejemplares(id_ejemplar) ON DELETE CASCADE
);

-- Tabla de Reservas (ahora se reserva una copia específica)
CREATE TABLE Reservas (
    id_reserva INT PRIMARY KEY AUTO_INCREMENT,
    id_usuario INT NOT NULL,
    id_copia INT NOT NULL, -- Ahora se refiere a la copia específica
    fecha_reserva DATE NOT NULL DEFAULT (CURRENT_DATE),
    FOREIGN KEY (id_usuario) REFERENCES Usuarios(id_usuario),
    FOREIGN KEY (id_copia) REFERENCES Copias(id_copia) ON DELETE CASCADE
);

-- Tabla de Préstamos (ahora se presta una copia específica)
CREATE TABLE Prestamos (
    id_prestamo INT PRIMARY KEY AUTO_INCREMENT,
    id_usuario INT NOT NULL,
    id_copia INT NOT NULL, -- Ahora se refiere a la copia específica
    fecha_prestamo DATE NOT NULL DEFAULT (CURRENT_DATE),
    estado ENUM('Activo', 'Devuelto') DEFAULT 'Activo',
    fecha_devolucion DATE NULL, -- Se actualiza al devolver
    FOREIGN KEY (id_usuario) REFERENCES Usuarios(id_usuario),
    FOREIGN KEY (id_copia) REFERENCES Copias(id_copia) ON DELETE CASCADE
);

-- Tabla específica para Libros (referencia a Editorial y Género)
CREATE TABLE Libros (
    id_ejemplar INT PRIMARY KEY,
    isbn VARCHAR(20),
    id_editorial INT, -- Referencia al catálogo de Editoriales
    id_genero INT,    -- Referencia al catálogo de Géneros
    edicion INT,
    FOREIGN KEY (id_ejemplar) REFERENCES Ejemplares(id_ejemplar) ON DELETE CASCADE,
    FOREIGN KEY (id_editorial) REFERENCES Editoriales(id_editorial),
    FOREIGN KEY (id_genero) REFERENCES Generos(id_genero)
);

-- Tabla específica para Diccionarios
CREATE TABLE Diccionarios (
    id_ejemplar INT PRIMARY KEY,
    idioma VARCHAR(50),
    volumen INT,
    FOREIGN KEY (id_ejemplar) REFERENCES Ejemplares(id_ejemplar) ON DELETE CASCADE
);

-- Tabla específica para Mapas
CREATE TABLE Mapas (
    id_ejemplar INT PRIMARY KEY,
    escala VARCHAR(50),
    tipo_mapa VARCHAR(100), -- físico, político, etc.
    FOREIGN KEY (id_ejemplar) REFERENCES Ejemplares(id_ejemplar) ON DELETE CASCADE
);

-- Tabla específica para Tesis
CREATE TABLE Tesis (
    id_ejemplar INT PRIMARY KEY,
    grado_academico VARCHAR(100), -- Licenciatura, Maestría, etc.
    facultad VARCHAR(100),
    FOREIGN KEY (id_ejemplar) REFERENCES Ejemplares(id_ejemplar) ON DELETE CASCADE
);

-- Tabla específica para DVDs (referencia a Género)
CREATE TABLE DVDs (
    id_ejemplar INT PRIMARY KEY,
    duracion TIME,
    id_genero INT, -- Referencia al catálogo de Géneros
    FOREIGN KEY (id_ejemplar) REFERENCES Ejemplares(id_ejemplar) ON DELETE CASCADE,
    FOREIGN KEY (id_genero) REFERENCES Generos(id_genero)
);

-- Tabla específica para VHS (referencia a Género)
CREATE TABLE VHS (
    id_ejemplar INT PRIMARY KEY,
    duracion TIME,
    id_genero INT, -- Referencia al catálogo de Géneros
    FOREIGN KEY (id_ejemplar) REFERENCES Ejemplares(id_ejemplar) ON DELETE CASCADE,
    FOREIGN KEY (id_genero) REFERENCES Generos(id_genero)
);

-- Tabla específica para Cassettes (referencia a Tipo de Cinta)
CREATE TABLE Cassettes (
    id_ejemplar INT PRIMARY KEY,
    duracion TIME,
    id_tipo_cinta INT, -- Referencia al catálogo de Tipos de Cinta
    FOREIGN KEY (id_ejemplar) REFERENCES Ejemplares(id_ejemplar) ON DELETE CASCADE,
    FOREIGN KEY (id_tipo_cinta) REFERENCES TiposCinta(id_tipo_cinta)
);

-- Tabla específica para CDs (referencia a Género)
CREATE TABLE CDs (
    id_ejemplar INT PRIMARY KEY,
    duracion TIME,
    id_genero INT, -- Referencia al catálogo de Géneros
    FOREIGN KEY (id_ejemplar) REFERENCES Ejemplares(id_ejemplar) ON DELETE CASCADE,
    FOREIGN KEY (id_genero) REFERENCES Generos(id_genero)
);

-- Tabla específica para Documentos (referencia a Tipo de Documento Detalle)
CREATE TABLE Documentos (
    id_ejemplar INT PRIMARY KEY,
    id_tipo_detalle INT, -- Referencia al catálogo de Tipos de Documento Detalle
    FOREIGN KEY (id_ejemplar) REFERENCES Ejemplares(id_ejemplar) ON DELETE CASCADE,
    FOREIGN KEY (id_tipo_detalle) REFERENCES TiposDocumentoDetalle(id_tipo_detalle)
);

-- Tabla específica para Periódicos (referencia a Tipo de Periódico)
CREATE TABLE Periodicos (
    id_ejemplar INT PRIMARY KEY,
    fecha_publicacion DATE,
    id_tipo_periodico INT, -- Referencia al catálogo de Tipos de Periódico
    FOREIGN KEY (id_ejemplar) REFERENCES Ejemplares(id_ejemplar) ON DELETE CASCADE,
    FOREIGN KEY (id_tipo_periodico) REFERENCES TiposPeriodico(id_tipo_periodico)
);

-- Tabla específica para Revistas (referencia a Tipo de Revista y Género)
CREATE TABLE Revistas (
    id_ejemplar INT PRIMARY KEY,
    fecha_publicacion DATE,
    id_tipo_revista INT, -- Referencia al catálogo de Tipos de Revista
    id_genero INT, -- Referencia al catálogo de Géneros
    FOREIGN KEY (id_ejemplar) REFERENCES Ejemplares(id_ejemplar) ON DELETE CASCADE,
    FOREIGN KEY (id_tipo_revista) REFERENCES TiposRevista(id_tipo_revista),
    FOREIGN KEY (id_genero) REFERENCES Generos(id_genero)
);

-- Insertar roles
INSERT INTO Roles (nombre_rol, cant_max_prestamo, dias_prestamo, mora_diaria)
VALUES
    ('Administrador', 0, 0, 0), -- 0 indica sin límite o sin préstamo
    ('Profesor', 6, 15, 0.10),     -- 6 libros, 15 días
    ('Alumno', 3, 7, 0.10);        -- 3 libros, 7 días

-- Ejemplo de inserción de datos en catálogos
INSERT INTO Autores (nombre_autor) VALUES ('Autor Ejemplo');
INSERT INTO Editoriales (nombre_editorial) VALUES ('Editorial Ejemplo');
INSERT INTO Generos (nombre_genero) VALUES ('Ficción');
INSERT INTO TiposDocumentoDetalle (nombre_tipo_detalle) VALUES ('Informe'), ('Memorando'), ('Contrato');
INSERT INTO TiposPeriodico (nombre_tipo_periodico) VALUES ('Local'), ('Nacional'), ('Internacional');
INSERT INTO TiposRevista (nombre_tipo_revista) VALUES ('Científica'), ('Cultural'), ('Tecnológica');
INSERT INTO TiposCinta (nombre_tipo_cinta) VALUES ('Audio'), ('Video');

-- Ejemplo de inserción de un ejemplar (Documento)
INSERT INTO Ejemplares (titulo, id_autor, ubicacion, tipo_documento) 
VALUES ('Informe Anual 2024', 1, 'Archivo B', 'Documento');

-- Asociar el ejemplar con información específica de Documento
INSERT INTO Documentos (id_ejemplar, id_tipo_detalle) 
VALUES (1, 1); -- Asocia con 'Informe' (id_tipo_detalle = 1)

-- Inserción de copias para ese ejemplar
INSERT INTO Copias (id_ejemplar, codigo_unico) VALUES (1, 'DOC-001');
INSERT INTO Copias (id_ejemplar, codigo_unico) VALUES (1, 'DOC-002');
-- ... (más copias si se desean)
