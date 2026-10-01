-- =====================================================================
-- Sistema de Quejas Bancario - Extensiones necesarias sobre la base de
-- datos ya creada en Neon (a partir del documento "09_Base_de_Datos").
--
-- Ejecuta este script UNA VEZ contra tu base de datos de Neon (por
-- ejemplo desde el SQL Editor de la consola de Neon, o con psql) antes
-- de levantar el backend. Es seguro volver a ejecutarlo: todas las
-- sentencias usan IF NOT EXISTS / ON CONFLICT.
--
-- Justificacion de cada cambio: el documento de Casos de Uso (CU-01,
-- CU-02, CU-06, CU-08, RN06, RN07) exige capturar/almacenar datos que
-- la tabla original no contemplaba con un campo dedicado.
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1) USUARIO: disponibilidad de sesion (RN06 - criterio de asignacion
--    automatica) y bloqueo temporal por intentos fallidos (RNF02).
-- ---------------------------------------------------------------------
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS sesion_activa BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS bloqueado_hasta TIMESTAMP NULL;

-- ---------------------------------------------------------------------
-- 2) CASO: datos de contacto capturados en el registro (CU-02 / RN04),
--    detalle de la resolucion (CU-08) y contador de reasignaciones
--    utilizadas (RN07: maximo 2 por caso).
-- ---------------------------------------------------------------------
ALTER TABLE caso ADD COLUMN IF NOT EXISTS nombre_cliente_caso VARCHAR(150);
ALTER TABLE caso ADD COLUMN IF NOT EXISTS identificacion_cliente VARCHAR(30);
ALTER TABLE caso ADD COLUMN IF NOT EXISTS correo_contacto VARCHAR(150);
ALTER TABLE caso ADD COLUMN IF NOT EXISTS telefono_contacto VARCHAR(20);
ALTER TABLE caso ADD COLUMN IF NOT EXISTS detalle_resolucion TEXT;
ALTER TABLE caso ADD COLUMN IF NOT EXISTS solicitudes_reasignacion_usadas INT NOT NULL DEFAULT 0;

-- ---------------------------------------------------------------------
-- 3) SOLICITUD_REASIGNACION: motivo de rechazo (CU-06, flujo alterno).
-- ---------------------------------------------------------------------
ALTER TABLE solicitud_reasignacion ADD COLUMN IF NOT EXISTS motivo_rechazo TEXT;

-- ---------------------------------------------------------------------
-- 4) Nueva tabla: recuperacion de contrasena (CU-01, 2.3.3).
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS password_reset_token (
    id_token          BIGSERIAL PRIMARY KEY,
    id_usuario        INT NOT NULL REFERENCES usuario(id_usuario),
    token             VARCHAR(255) NOT NULL UNIQUE,
    fecha_expiracion  TIMESTAMP NOT NULL,
    usado             BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_creacion    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------------
-- 5) Datos de catalogo (por si tu base aun no los tiene cargados).
-- ---------------------------------------------------------------------
INSERT INTO rol (nombre_rol)
    VALUES ('Cliente'), ('Agente'), ('Administrador'), ('Supervisor'), ('Auditor')
    ON CONFLICT (nombre_rol) DO NOTHING;

INSERT INTO tipo_caso (nombre, prefijo)
    VALUES ('Queja', 'Q'), ('Reclamo', 'R'), ('Denuncia', 'D'), ('Sugerencia', 'S')
    ON CONFLICT (nombre) DO NOTHING;

INSERT INTO parametro_sistema (nombre_parametro, valor)
    VALUES ('limite_casos_activos_agente', '10')
    ON CONFLICT (nombre_parametro) DO NOTHING;

-- ---------------------------------------------------------------------
-- 6) (Opcional) Usuario Administrador inicial. Genera el hash BCrypt con
--    la utilidad incluida en el backend antes de descomentar y ejecutar:
--
--    mvn exec:java -Dexec.mainClass="com.umg.quejasbancario.util.PasswordHashGenerator" -Dexec.args="TuContrasenaSegura"
--
-- INSERT INTO usuario (nombre_usuario, nombre_completo, correo_electronico, contrasena_hash, id_rol, estado)
-- VALUES (
--     'admin',
--     'Administrador del Sistema',
--     'admin@quejasbancario.com',
--     '<HASH_BCRYPT_AQUI>',
--     (SELECT id_rol FROM rol WHERE nombre_rol = 'Administrador'),
--     'Activo'
-- )
-- ON CONFLICT (nombre_usuario) DO NOTHING;

-- ---------------------------------------------------------------------
-- 7) (Recomendado) Triggers de inmutabilidad de bitacora a nivel de
--    motor de datos (RN15), si tu script original en Neon no los
--    incluyo todavia (MySQL y PostgreSQL usan sintaxis distinta).
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

DROP TRIGGER IF EXISTS trg_bitcaso_no_update ON bitacora_caso;
CREATE TRIGGER trg_bitcaso_no_update BEFORE UPDATE ON bitacora_caso
    FOR EACH ROW EXECUTE FUNCTION fn_bitacora_no_update();
DROP TRIGGER IF EXISTS trg_bitcaso_no_delete ON bitacora_caso;
CREATE TRIGGER trg_bitcaso_no_delete BEFORE DELETE ON bitacora_caso
    FOR EACH ROW EXECUTE FUNCTION fn_bitacora_no_delete();

DROP TRIGGER IF EXISTS trg_bitacceso_no_update ON bitacora_acceso;
CREATE TRIGGER trg_bitacceso_no_update BEFORE UPDATE ON bitacora_acceso
    FOR EACH ROW EXECUTE FUNCTION fn_bitacora_no_update();
DROP TRIGGER IF EXISTS trg_bitacceso_no_delete ON bitacora_acceso;
CREATE TRIGGER trg_bitacceso_no_delete BEFORE DELETE ON bitacora_acceso
    FOR EACH ROW EXECUTE FUNCTION fn_bitacora_no_delete();

DROP TRIGGER IF EXISTS trg_bitusr_no_update ON bitacora_usuario;
CREATE TRIGGER trg_bitusr_no_update BEFORE UPDATE ON bitacora_usuario
    FOR EACH ROW EXECUTE FUNCTION fn_bitacora_no_update();
DROP TRIGGER IF EXISTS trg_bitusr_no_delete ON bitacora_usuario;
CREATE TRIGGER trg_bitusr_no_delete BEFORE DELETE ON bitacora_usuario
    FOR EACH ROW EXECUTE FUNCTION fn_bitacora_no_delete();

DROP TRIGGER IF EXISTS trg_bitcorreo_no_update ON bitacora_correo;
CREATE TRIGGER trg_bitcorreo_no_update BEFORE UPDATE ON bitacora_correo
    FOR EACH ROW EXECUTE FUNCTION fn_bitacora_no_update();
DROP TRIGGER IF EXISTS trg_bitcorreo_no_delete ON bitacora_correo;
CREATE TRIGGER trg_bitcorreo_no_delete BEFORE DELETE ON bitacora_correo
    FOR EACH ROW EXECUTE FUNCTION fn_bitacora_no_delete();
