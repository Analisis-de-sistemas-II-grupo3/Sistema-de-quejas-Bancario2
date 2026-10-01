-- =====================================================================
-- Sistema de Quejas Bancario - Script de Base de Datos (PostgreSQL / Neon)
-- Adaptado del script original en MySQL/MariaDB (Capitulo 9: Base de Datos)
-- =====================================================================
-- Este script es de REFERENCIA: recrea desde cero el mismo modelo que ya
-- tienes en Neon, pero adaptado a la sintaxis de PostgreSQL (SERIAL en
-- lugar de AUTO_INCREMENT, sin ENUM nativo -> VARCHAR + CHECK, TIMESTAMP
-- en lugar de DATETIME, triggers en PL/pgSQL en lugar de sintaxis MySQL).
--
-- Si tu base de datos en Neon ya existe con el modelo del documento
-- "09_Base_de_Datos", NO necesitas correr este archivo: en su lugar
-- ejecuta unicamente database/extensions.sql, que agrega las columnas y
-- tabla adicionales que el backend requiere (ver README.md, seccion
-- "Supuestos y extensiones a la base de datos").
--
-- Este archivo se deja como referencia completa, por si prefieres crear
-- la base desde cero en un esquema/proyecto nuevo de Neon.
-- =====================================================================

-- ---------------------------------------------------------------------
-- Tablas de catalogo y control de acceso
-- ---------------------------------------------------------------------

CREATE TABLE rol (
    id_rol      SERIAL PRIMARY KEY,
    nombre_rol  VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE usuario (
    id_usuario          SERIAL PRIMARY KEY,
    nombre_usuario      VARCHAR(50) NOT NULL UNIQUE,
    nombre_completo     VARCHAR(150) NOT NULL,
    correo_electronico  VARCHAR(150) NOT NULL UNIQUE,
    contrasena_hash     VARCHAR(255) NOT NULL,
    id_rol              INT NOT NULL REFERENCES rol(id_rol),
    estado              VARCHAR(20) NOT NULL DEFAULT 'Activo'
                            CHECK (estado IN ('Activo','Inactivo')),
    intentos_fallidos   INT NOT NULL DEFAULT 0,
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- Extensiones requeridas por el backend (ver extensions.sql):
    sesion_activa       BOOLEAN NOT NULL DEFAULT FALSE,
    bloqueado_hasta     TIMESTAMP NULL
);

CREATE TABLE cuenta_bancaria (
    id_cuenta      SERIAL PRIMARY KEY,
    numero_cuenta  VARCHAR(30) NOT NULL UNIQUE,
    id_usuario     INT NOT NULL REFERENCES usuario(id_usuario),
    estado         VARCHAR(20) NOT NULL DEFAULT 'Activa'
                       CHECK (estado IN ('Activa','Inactiva'))
);

CREATE TABLE tipo_caso (
    id_tipo_caso  SERIAL PRIMARY KEY,
    nombre        VARCHAR(30) NOT NULL UNIQUE,
    prefijo       CHAR(1) NOT NULL UNIQUE
);

CREATE TABLE categoria (
    id_categoria  SERIAL PRIMARY KEY,
    nombre        VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE producto_servicio (
    id_producto  SERIAL PRIMARY KEY,
    nombre       VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE parametro_sistema (
    id_parametro      SERIAL PRIMARY KEY,
    nombre_parametro  VARCHAR(100) NOT NULL UNIQUE,
    valor             VARCHAR(50) NOT NULL
);

-- Extension: recuperacion de contrasena (CU-01, 2.3.3)
CREATE TABLE password_reset_token (
    id_token          BIGSERIAL PRIMARY KEY,
    id_usuario        INT NOT NULL REFERENCES usuario(id_usuario),
    token             VARCHAR(255) NOT NULL UNIQUE,
    fecha_expiracion  TIMESTAMP NOT NULL,
    usado             BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_creacion    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------------
-- Nucleo del sistema: casos
-- ---------------------------------------------------------------------

CREATE TABLE caso (
    id_caso             SERIAL PRIMARY KEY,
    numero_caso         VARCHAR(20) NOT NULL UNIQUE,
    id_tipo_caso        INT NOT NULL REFERENCES tipo_caso(id_tipo_caso),
    id_categoria        INT NULL REFERENCES categoria(id_categoria),
    id_producto         INT NULL REFERENCES producto_servicio(id_producto),
    id_cliente          INT NOT NULL REFERENCES usuario(id_usuario),
    id_cuenta           INT NOT NULL REFERENCES cuenta_bancaria(id_cuenta),
    id_agente_asignado  INT NULL REFERENCES usuario(id_usuario),
    descripcion         TEXT NOT NULL,
    estado              VARCHAR(20) NOT NULL DEFAULT 'Registrado'
                            CHECK (estado IN ('Registrado','Asignado','En atención','En espera','Resuelto','Cerrado')),
    fecha_registro      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_cierre        TIMESTAMP NULL,
    -- Extensiones requeridas por el backend (ver extensions.sql):
    nombre_cliente_caso              VARCHAR(150),
    identificacion_cliente           VARCHAR(30),
    correo_contacto                  VARCHAR(150),
    telefono_contacto                VARCHAR(20),
    detalle_resolucion               TEXT,
    solicitudes_reasignacion_usadas  INT NOT NULL DEFAULT 0
);

CREATE TABLE documento_adjunto (
    id_documento      SERIAL PRIMARY KEY,
    id_caso           INT NOT NULL REFERENCES caso(id_caso),
    nombre_archivo    VARCHAR(255) NOT NULL,
    ruta_archivo      VARCHAR(500) NOT NULL,
    tamano            INT NOT NULL,
    id_usuario_carga  INT NOT NULL REFERENCES usuario(id_usuario),
    fecha_carga       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE solicitud_reasignacion (
    id_solicitud             SERIAL PRIMARY KEY,
    id_caso                  INT NOT NULL REFERENCES caso(id_caso),
    id_agente_solicita       INT NOT NULL REFERENCES usuario(id_usuario),
    id_supervisor_resuelve   INT NULL REFERENCES usuario(id_usuario),
    motivo                   TEXT NOT NULL,
    estado                   VARCHAR(20) NOT NULL DEFAULT 'Pendiente'
                                 CHECK (estado IN ('Pendiente','Aprobada','Rechazada')),
    fecha_solicitud          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_resolucion         TIMESTAMP NULL,
    -- Extension requerida por el backend (ver extensions.sql):
    motivo_rechazo           TEXT
);

-- ---------------------------------------------------------------------
-- Bitacoras (RN15) - inmutables, ver triggers al final del script
-- ---------------------------------------------------------------------

CREATE TABLE bitacora_caso (
    id_bitacora_caso    BIGSERIAL PRIMARY KEY,
    id_caso             INT NOT NULL REFERENCES caso(id_caso),
    id_usuario          INT NULL REFERENCES usuario(id_usuario),
    rol_ejecuta         VARCHAR(30) NOT NULL,
    ip                  VARCHAR(45) NOT NULL,
    estado_anterior     VARCHAR(30) NULL,
    estado_nuevo        VARCHAR(30) NOT NULL,
    descripcion_evento  TEXT NOT NULL,
    fecha_hora          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE bitacora_acceso (
    id_bitacora_acceso  BIGSERIAL PRIMARY KEY,
    id_usuario          INT NOT NULL REFERENCES usuario(id_usuario),
    ip                  VARCHAR(45) NOT NULL,
    tipo_evento         VARCHAR(30) NOT NULL
                            CHECK (tipo_evento IN ('Inicio de sesión','Cierre de sesión')),
    descripcion_evento  TEXT NOT NULL,
    fecha_hora          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE bitacora_usuario (
    id_bitacora_usuario  BIGSERIAL PRIMARY KEY,
    id_usuario_afectado  INT NOT NULL REFERENCES usuario(id_usuario),
    id_usuario_ejecuta   INT NOT NULL REFERENCES usuario(id_usuario),
    motivo               VARCHAR(255) NOT NULL,
    descripcion_evento   TEXT NOT NULL,
    fecha_hora           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE bitacora_correo (
    id_bitacora_correo  BIGSERIAL PRIMARY KEY,
    id_caso             INT NULL REFERENCES caso(id_caso),
    destinatario        VARCHAR(150) NOT NULL,
    tipo_notificacion   VARCHAR(100) NOT NULL,
    descripcion_evento  TEXT NOT NULL,
    fecha_hora          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------------
-- Indices recomendados (busquedas frecuentes, RN14 / RF30)
-- ---------------------------------------------------------------------

CREATE INDEX idx_caso_estado   ON caso(estado);
CREATE INDEX idx_caso_agente   ON caso(id_agente_asignado);
CREATE INDEX idx_caso_cliente  ON caso(id_cliente);
CREATE INDEX idx_caso_fecha    ON caso(fecha_registro);
CREATE INDEX idx_bitcaso_caso      ON bitacora_caso(id_caso);
CREATE INDEX idx_bitacceso_usuario ON bitacora_acceso(id_usuario);
CREATE INDEX idx_bitcorreo_caso    ON bitacora_correo(id_caso);

-- ---------------------------------------------------------------------
-- Triggers de inmutabilidad de bitacora (RN15 / RN02)
-- Ningun registro de bitacora puede ser modificado ni eliminado,
-- ni siquiera por el Administrador. Se aplica a nivel de motor de datos.
-- ---------------------------------------------------------------------

CREATE OR REPLACE FUNCTION fn_bitacora_no_update() RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'La bitácora es inmutable: no se permite modificar registros.';
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION fn_bitacora_no_delete() RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'La bitácora es inmutable: no se permite eliminar registros.';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_bitcaso_no_update BEFORE UPDATE ON bitacora_caso
    FOR EACH ROW EXECUTE FUNCTION fn_bitacora_no_update();
CREATE TRIGGER trg_bitcaso_no_delete BEFORE DELETE ON bitacora_caso
    FOR EACH ROW EXECUTE FUNCTION fn_bitacora_no_delete();

CREATE TRIGGER trg_bitacceso_no_update BEFORE UPDATE ON bitacora_acceso
    FOR EACH ROW EXECUTE FUNCTION fn_bitacora_no_update();
CREATE TRIGGER trg_bitacceso_no_delete BEFORE DELETE ON bitacora_acceso
    FOR EACH ROW EXECUTE FUNCTION fn_bitacora_no_delete();

CREATE TRIGGER trg_bitusr_no_update BEFORE UPDATE ON bitacora_usuario
    FOR EACH ROW EXECUTE FUNCTION fn_bitacora_no_update();
CREATE TRIGGER trg_bitusr_no_delete BEFORE DELETE ON bitacora_usuario
    FOR EACH ROW EXECUTE FUNCTION fn_bitacora_no_delete();

CREATE TRIGGER trg_bitcorreo_no_update BEFORE UPDATE ON bitacora_correo
    FOR EACH ROW EXECUTE FUNCTION fn_bitacora_no_update();
CREATE TRIGGER trg_bitcorreo_no_delete BEFORE DELETE ON bitacora_correo
    FOR EACH ROW EXECUTE FUNCTION fn_bitacora_no_delete();

-- ---------------------------------------------------------------------
-- Datos iniciales de catalogo
-- ---------------------------------------------------------------------

INSERT INTO rol (nombre_rol) VALUES
    ('Cliente'), ('Agente'), ('Administrador'), ('Supervisor'), ('Auditor');

INSERT INTO tipo_caso (nombre, prefijo) VALUES
    ('Queja', 'Q'), ('Reclamo', 'R'), ('Denuncia', 'D'), ('Sugerencia', 'S');

INSERT INTO parametro_sistema (nombre_parametro, valor) VALUES
    ('limite_casos_activos_agente', '10');

-- ---------------------------------------------------------------------
-- Usuario Administrador inicial.
-- IMPORTANTE: contrasena_hash NO puede ser texto plano, debe ser un hash
-- BCrypt. Genera el tuyo ejecutando la clase utilitaria incluida:
--   com.umg.quejasbancario.util.PasswordHashGenerator "TuContrasenaSegura"
-- (ver README.md, seccion "Crear el usuario Administrador inicial") y
-- reemplaza <HASH_BCRYPT_AQUI> por el valor que te imprima en consola.
-- ---------------------------------------------------------------------

INSERT INTO usuario (nombre_usuario, nombre_completo, correo_electronico, contrasena_hash, id_rol, estado)
VALUES (
    'admin',
    'Administrador del Sistema',
    'admin@quejasbancario.com',
    '<HASH_BCRYPT_AQUI>',
    (SELECT id_rol FROM rol WHERE nombre_rol = 'Administrador'),
    'Activo'
);
