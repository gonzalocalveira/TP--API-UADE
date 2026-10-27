-- =============================================================================
-- 01_schema.sql  -  Modelo de datos de GestionDeCine (MySQL 8)
--
-- Crea la base "gestioncine" desde cero con todas sus tablas y restricciones.
-- Correrlo de nuevo RESETEA la base (borra todo y la vuelve a crear).
-- Despues de este script se corre 02_datos.sql para cargar datos de prueba.
--
-- La aplicacion Spring NO crea ni modifica tablas (ddl-auto=validate): solo
-- verifica al arrancar que estas tablas coincidan con las clases @Entity.
--
-- Convenciones:
-- - Toda tabla tiene una clave primaria "id" INT AUTO_INCREMENT: es una clave
--   subrogada (no tiene significado de negocio), nunca cambia y es chica, por
--   eso es ideal para referenciarla desde otras tablas.
-- - Las columnas de enums de Java (@Enumerated(EnumType.STRING)) son ENUM de
--   MySQL: la base solo acepta los valores validos y Hibernate valida ese tipo.
-- - Dinero en DECIMAL(10,2): exacto, sin errores de redondeo (en Java BigDecimal).
-- - ON DELETE RESTRICT: la base impide borrar un registro que todavia esta
--   referenciado. La API ademas lo chequea antes y responde 409 Conflict.
-- =============================================================================

DROP DATABASE IF EXISTS gestioncine;
CREATE DATABASE gestioncine CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE gestioncine;

-- -----------------------------------------------------------------------------
-- usuarios: personas con acceso al sistema (entidad Usuario).
-- mail y dni son UNIQUE porque identifican a una persona: no puede haber dos
-- cuentas con el mismo mail o el mismo DNI. No se usan como PK porque pueden
-- cambiar (el usuario puede editar su mail) y las FK quedarian desactualizadas.
-- -----------------------------------------------------------------------------
CREATE TABLE usuarios (
    id               INT          NOT NULL AUTO_INCREMENT,
    nombre_apellido  VARCHAR(100) NOT NULL,
    dni              INT          NOT NULL,
    fecha_nacimiento DATE         NOT NULL,
    mail             VARCHAR(100) NOT NULL,
    password         VARCHAR(100) NOT NULL,
    is_socio         BIT(1)       NOT NULL DEFAULT b'0',
    rol              ENUM('CLIENTE','EMPLEADO','ADMINISTRADOR') NOT NULL,
    CONSTRAINT pk_usuarios PRIMARY KEY (id),
    CONSTRAINT uk_usuarios_mail UNIQUE (mail),
    CONSTRAINT uk_usuarios_dni UNIQUE (dni),
    CONSTRAINT chk_usuarios_dni CHECK (dni > 0)
);

-- -----------------------------------------------------------------------------
-- membresia: programa de socios (entidad Membresia). Relacion 1 a 1 con
-- usuarios: la FK usuario_id es UNIQUE, entonces un usuario tiene como maximo
-- una membresia.
-- ON DELETE CASCADE: la membresia no tiene sentido sin su usuario; si se borra
-- el usuario, la base borra su membresia automaticamente.
-- -----------------------------------------------------------------------------
CREATE TABLE membresia (
    id             INT NOT NULL AUTO_INCREMENT,
    usuario_id     INT NOT NULL,
    puntos         INT NOT NULL DEFAULT 0,
    tipo_membresia ENUM('CLASSIC','GOLD','BLACK') NOT NULL,
    CONSTRAINT pk_membresia PRIMARY KEY (id),
    CONSTRAINT uk_membresia_usuario UNIQUE (usuario_id),
    CONSTRAINT fk_membresia_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios (id) ON DELETE CASCADE,
    CONSTRAINT chk_membresia_puntos CHECK (puntos >= 0)
);

-- -----------------------------------------------------------------------------
-- pelicula: catalogo de peliculas (entidad Pelicula).
-- duracion (minutos) se usa para calcular si dos funciones se superponen.
-- -----------------------------------------------------------------------------
CREATE TABLE pelicula (
    id                 INT           NOT NULL AUTO_INCREMENT,
    nombre             VARCHAR(150)  NOT NULL,
    director           VARCHAR(100)  NOT NULL,
    duracion           INT           NOT NULL,
    fecha_estreno      DATE          NOT NULL,
    idioma             VARCHAR(50)   NOT NULL,
    genero             VARCHAR(50)   NOT NULL,
    clasificacion_edad VARCHAR(20)   NOT NULL,
    sinopsis           VARCHAR(1000) NOT NULL,
    CONSTRAINT pk_pelicula PRIMARY KEY (id),
    CONSTRAINT chk_pelicula_duracion CHECK (duracion > 0)
);

-- -----------------------------------------------------------------------------
-- sala: salas fisicas del cine (entidad Sala).
-- nombre UNIQUE: es como el "numero de sala"; dos salas con el mismo nombre
-- serian imposibles de distinguir para el publico.
-- -----------------------------------------------------------------------------
CREATE TABLE sala (
    id        INT         NOT NULL AUTO_INCREMENT,
    nombre    VARCHAR(50) NOT NULL,
    capacidad INT         NOT NULL,
    CONSTRAINT pk_sala PRIMARY KEY (id),
    CONSTRAINT uk_sala_nombre UNIQUE (nombre),
    CONSTRAINT chk_sala_capacidad CHECK (capacidad > 0)
);

-- -----------------------------------------------------------------------------
-- asiento: butacas fisicas de cada sala (entidad Asiento). N a 1 con sala.
-- UNIQUE (sala_id, fila_asiento, numero_asiento): no puede haber dos butacas
-- "A5" en la misma sala, aunque si una "A5" en cada sala.
-- ON DELETE RESTRICT: no se puede borrar una sala que todavia tiene butacas.
-- -----------------------------------------------------------------------------
CREATE TABLE asiento (
    id             INT        NOT NULL AUTO_INCREMENT,
    numero_asiento INT        NOT NULL,
    fila_asiento   VARCHAR(5) NOT NULL,
    sala_id        INT        NOT NULL,
    CONSTRAINT pk_asiento PRIMARY KEY (id),
    CONSTRAINT uk_asiento_ubicacion UNIQUE (sala_id, fila_asiento, numero_asiento),
    CONSTRAINT fk_asiento_sala FOREIGN KEY (sala_id)
        REFERENCES sala (id) ON DELETE RESTRICT,
    CONSTRAINT chk_asiento_numero CHECK (numero_asiento > 0)
);

-- -----------------------------------------------------------------------------
-- funcion: una proyeccion (entidad Funcion) = pelicula + sala + fecha/hora + precio.
-- N a 1 con pelicula y N a 1 con sala.
-- ON DELETE RESTRICT en ambas: no se puede borrar una pelicula o una sala que
-- tiene funciones programadas (se perderia la historia de lo vendido).
-- -----------------------------------------------------------------------------
CREATE TABLE funcion (
    id              INT           NOT NULL AUTO_INCREMENT,
    fecha_funcion   DATE          NOT NULL,
    hora_funcion    TIME          NOT NULL,
    formato_funcion ENUM('DOS_D','TRES_D','CUATRO_DX') NOT NULL,
    precio          DECIMAL(10,2) NOT NULL,
    pelicula_id     INT           NOT NULL,
    sala_id         INT           NOT NULL,
    CONSTRAINT pk_funcion PRIMARY KEY (id),
    CONSTRAINT fk_funcion_pelicula FOREIGN KEY (pelicula_id)
        REFERENCES pelicula (id) ON DELETE RESTRICT,
    CONSTRAINT fk_funcion_sala FOREIGN KEY (sala_id)
        REFERENCES sala (id) ON DELETE RESTRICT,
    CONSTRAINT chk_funcion_precio CHECK (precio > 0)
);

-- -----------------------------------------------------------------------------
-- ticket_compra: una compra (entidad TicketCompra) de un usuario para una
-- funcion. Agrupa varias entradas (1 a N con entrada).
-- total lo calcula el servidor (precio de la funcion x cantidad de butacas).
-- estado: CONFIRMADA o CANCELADA. Las compras no se borran, se cancelan
-- (asi queda el historial).
-- ON DELETE RESTRICT: no se puede borrar un usuario o una funcion con compras.
-- -----------------------------------------------------------------------------
CREATE TABLE ticket_compra (
    id           INT           NOT NULL AUTO_INCREMENT,
    fecha_compra DATETIME(6)   NOT NULL,
    total        DECIMAL(10,2) NOT NULL,
    estado       ENUM('CONFIRMADA','CANCELADA') NOT NULL,
    usuario_id   INT           NOT NULL,
    funcion_id   INT           NOT NULL,
    CONSTRAINT pk_ticket_compra PRIMARY KEY (id),
    CONSTRAINT fk_ticket_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios (id) ON DELETE RESTRICT,
    CONSTRAINT fk_ticket_funcion FOREIGN KEY (funcion_id)
        REFERENCES funcion (id) ON DELETE RESTRICT,
    CONSTRAINT chk_ticket_total CHECK (total > 0)
);

-- -----------------------------------------------------------------------------
-- entrada: una butaca dentro de una compra (entidad Entrada).
-- N a 1 con ticket_compra y N a 1 con asiento. La funcion se obtiene por el
-- ticket (no se repite el dato aca: normalizacion).
-- ON DELETE CASCADE hacia ticket_compra: una entrada no existe sin su compra.
-- ON DELETE RESTRICT hacia asiento: no se puede borrar una butaca ya vendida.
-- -----------------------------------------------------------------------------
CREATE TABLE entrada (
    id               INT           NOT NULL AUTO_INCREMENT,
    estado           ENUM('OCUPADA','CANCELADA') NOT NULL,
    precio_unitario  DECIMAL(10,2) NOT NULL,
    ticket_compra_id INT           NOT NULL,
    asiento_id       INT           NOT NULL,
    CONSTRAINT pk_entrada PRIMARY KEY (id),
    CONSTRAINT fk_entrada_ticket FOREIGN KEY (ticket_compra_id)
        REFERENCES ticket_compra (id) ON DELETE CASCADE,
    CONSTRAINT fk_entrada_asiento FOREIGN KEY (asiento_id)
        REFERENCES asiento (id) ON DELETE RESTRICT,
    CONSTRAINT chk_entrada_precio CHECK (precio_unitario > 0)
);
