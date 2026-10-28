-- =============================================================================
-- 02_datos.sql  -  Datos de prueba para la demo
--
-- Se corre DESPUES de 01_schema.sql (que deja la base vacia). Como 01 borra y
-- recrea la base, correr 01 + 02 de nuevo siempre deja los mismos datos, sin
-- duplicados.
-- Las funciones usan fechas relativas (DATE_ADD(CURDATE(), ...)) para que
-- siempre sean futuras y se puedan comprar entradas el dia de la demo.
-- =============================================================================

USE gestioncine;

-- Peliculas ------------------------------------------------------------------
INSERT INTO pelicula (nombre, director, duracion, fecha_estreno, idioma, genero, clasificacion_edad, sinopsis) VALUES
('Interstellar', 'Christopher Nolan', 169, '2014-11-06', 'Ingles (subtitulada)', 'Ciencia ficcion', '+13',
 'Un grupo de exploradores viaja a traves de un agujero de gusano para buscar un nuevo hogar para la humanidad.'),
('Toy Story 3', 'Lee Unkrich', 103, '2010-06-17', 'Espanol (doblada)', 'Animacion', 'ATP',
 'Andy se va a la universidad y sus juguetes terminan en una guarderia con reglas propias.'),
('El secreto de sus ojos', 'Juan Jose Campanella', 129, '2009-08-13', 'Espanol', 'Drama', '+16',
 'Un empleado judicial retirado escribe una novela sobre un caso de asesinato que nunca pudo cerrar.'),
('Dune: Parte Dos', 'Denis Villeneuve', 166, '2024-02-29', 'Ingles (subtitulada)', 'Ciencia ficcion', '+13',
 'Paul Atreides se une a los Fremen y busca venganza contra quienes destruyeron a su familia.');

-- Salas ------------------------------------------------------------------------
INSERT INTO sala (nombre, capacidad) VALUES
('Sala 1', 40),
('Sala 2', 40);  -- capacidad 40 con 30 butacas cargadas: deja lugar para probar el alta de butacas

-- Butacas ----------------------------------------------------------------------
-- Se generan con un producto cartesiano (CROSS JOIN) entre una lista de filas y
-- una lista de numeros, en vez de escribir 70 INSERTs a mano.
-- El id se calcula a mano (posicion de la fila x butacas por fila + numero) porque
-- en un INSERT ... SELECT MySQL reserva los AUTO_INCREMENT en bloques y deja
-- huecos; asi los ids quedan seguidos y la demo es predecible.
-- Sala 1: filas A-E x numeros 1-8 = 40 butacas -> ids 1 a 40 (A1 = 1, A2 = 2, ..., E8 = 40).
INSERT INTO asiento (id, fila_asiento, numero_asiento, sala_id)
SELECT (f.orden - 1) * 8 + n.numero, f.fila, n.numero, 1
FROM (SELECT 'A' AS fila, 1 AS orden UNION ALL SELECT 'B', 2 UNION ALL SELECT 'C', 3
      UNION ALL SELECT 'D', 4 UNION ALL SELECT 'E', 5) f
CROSS JOIN (SELECT 1 AS numero UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
            UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8) n;

-- Sala 2: filas A-C x numeros 1-10 = 30 butacas -> ids 41 a 70 (A1 = 41, ..., C10 = 70).
INSERT INTO asiento (id, fila_asiento, numero_asiento, sala_id)
SELECT 40 + (f.orden - 1) * 10 + n.numero, f.fila, n.numero, 2
FROM (SELECT 'A' AS fila, 1 AS orden UNION ALL SELECT 'B', 2 UNION ALL SELECT 'C', 3) f
CROSS JOIN (SELECT 1 AS numero UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
            UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10) n;

-- Funciones ----------------------------------------------------------------------
-- Ninguna se superpone con otra de la misma sala (se tuvo en cuenta la duracion).
INSERT INTO funcion (fecha_funcion, hora_funcion, formato_funcion, precio, pelicula_id, sala_id) VALUES
(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '18:00:00', 'DOS_D',     4500.00, 1, 1),  -- 1: Interstellar, Sala 1 (termina 20:49)
(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '21:30:00', 'DOS_D',     4000.00, 2, 1),  -- 2: Toy Story 3, Sala 1
(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '19:00:00', 'DOS_D',     3800.00, 3, 2),  -- 3: El secreto de sus ojos, Sala 2
(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '20:00:00', 'TRES_D',    5500.00, 4, 2),  -- 4: Dune 2, Sala 2
(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '16:00:00', 'CUATRO_DX', 7000.00, 1, 1),  -- 5: Interstellar 4DX, Sala 1
(DATE_ADD(CURDATE(), INTERVAL 3 DAY), '15:00:00', 'DOS_D',     4000.00, 2, 2);  -- 6: Toy Story 3, Sala 2

-- Usuarios ---------------------------------------------------------------------
-- Las passwords van en texto plano porque la Etapa 1 no incluye Spring Security;
-- con seguridad se guardaria un hash BCrypt.
INSERT INTO usuarios (nombre_apellido, dni, fecha_nacimiento, mail, password, is_socio, rol) VALUES
('Ana Gomez',         30111222, '1990-04-12', 'ana@mail.com',   'ana1234',   b'1', 'CLIENTE'),        -- 1
('Bruno Diaz',        35222333, '1995-09-30', 'bruno@mail.com', 'bruno1234', b'0', 'CLIENTE'),        -- 2
('Admin del Cine',    20333444, '1985-01-01', 'admin@cine.com', 'admin1234', b'0', 'ADMINISTRADOR');  -- 3

-- Membresias -------------------------------------------------------------------
-- Ana es socia (is_socio = 1), por eso tiene membresia.
INSERT INTO membresia (usuario_id, puntos, tipo_membresia) VALUES
(1, 150, 'GOLD');
