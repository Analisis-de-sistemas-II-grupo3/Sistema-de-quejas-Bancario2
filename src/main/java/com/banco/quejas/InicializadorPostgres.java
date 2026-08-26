package com.banco.quejas;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Completa el esquema Neon y datos mínimos. Sus sentencias son seguras de repetir. */
@Configuration
public class InicializadorPostgres {
    @Bean
    @Order(0)
    ApplicationRunner prepararBaseDeDatos(JdbcTemplate jdbc, PasswordEncoder codificador) {
        return argumentos -> {
            jdbc.execute("ALTER TABLE caso ADD COLUMN IF NOT EXISTS sucursal_canal VARCHAR(150)");
            jdbc.execute("ALTER TABLE caso ADD COLUMN IF NOT EXISTS fecha_hecho DATE");
            jdbc.execute("ALTER TABLE caso ALTER COLUMN descripcion TYPE VARCHAR(2000)");
            jdbc.execute("ALTER TABLE producto_servicio ADD COLUMN IF NOT EXISTS id_categoria BIGINT REFERENCES categoria(id_categoria)");
            jdbc.execute("ALTER TABLE documento_adjunto ADD COLUMN IF NOT EXISTS tipo_contenido VARCHAR(100)");
            jdbc.execute("ALTER TABLE documento_adjunto ADD COLUMN IF NOT EXISTS contenido BYTEA");
            for (String rol : new String[]{"CLIENTE", "AGENTE", "SUPERVISOR", "ADMINISTRADOR", "AUDITOR"}) {
                jdbc.update("INSERT INTO rol(nombre_rol) VALUES (?) ON CONFLICT(nombre_rol) DO NOTHING", rol);
            }
            crearUsuario(jdbc, codificador, "ana.lopez", "Cliente#2026", "Ana López", "ana.lopez@correo.com", "CLIENTE");
            crearUsuario(jdbc, codificador, "carlos.perez", "Agente#2026", "Carlos Pérez", "carlos.perez@correo.com", "AGENTE");
            crearUsuario(jdbc, codificador, "sofia.ruiz", "Supervisor#2026", "Sofía Ruiz", "sofia.ruiz@correo.com", "SUPERVISOR");
            crearUsuario(jdbc, codificador, "admin", "Admin#2026", "Administrador del sistema", "admin@correo.com", "ADMINISTRADOR");
            crearUsuario(jdbc, codificador, "auditor1", "Auditor#2026", "José Martínez", "auditor1@correo.com", "AUDITOR");
            Long clienteId = jdbc.queryForObject("SELECT id_usuario FROM usuario WHERE nombre_usuario = 'ana.lopez'", Long.class);
            jdbc.update("INSERT INTO cuenta_bancaria(estado, numero_cuenta, id_usuario) VALUES (TRUE, '1234567890', ?) ON CONFLICT(numero_cuenta) DO NOTHING", clienteId);
            jdbc.update("UPDATE producto_servicio SET id_categoria = (SELECT id_categoria FROM categoria ORDER BY id_categoria LIMIT 1) WHERE id_categoria IS NULL");
        };
    }
    private void crearUsuario(JdbcTemplate jdbc, PasswordEncoder codificador, String usuario, String clave, String nombre, String correo, String rol) {
        jdbc.update("INSERT INTO usuario(contrasena_hash, correo_electronico, estado, fecha_creacion, intentos_fallidos, nombre_completo, nombre_usuario, id_rol) "
                + "SELECT ?, ?, TRUE, ?, 0, ?, ?, id_rol FROM rol WHERE nombre_rol = ? ON CONFLICT(nombre_usuario) DO NOTHING",
                codificador.encode(clave), correo, Timestamp.valueOf(LocalDateTime.now()), nombre, usuario, rol);
    }
}
