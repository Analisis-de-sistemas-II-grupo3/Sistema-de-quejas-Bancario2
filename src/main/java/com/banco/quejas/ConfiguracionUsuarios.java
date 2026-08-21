package com.banco.quejas;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Usuarios y clientes de referencia para desarrollo local de CU01. La base de datos
 * los sustituirá (ver README). Las contraseñas se almacenan cifradas (hash BCrypt);
 * las de este archivo son solo para pruebas locales, nunca para producción.
 */
@Configuration
public class ConfiguracionUsuarios {

    @Bean
    PasswordEncoder codificadorContrasenas() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    RepositorioClientes repositorioClientes() {
        return new RepositorioClientes(List.of(
                new Cliente("CLI-001", "Ana López", "1234567890101", "ana.lopez@correo.com", "5555-0101",
                        List.of(new CuentaBancaria("1234567890", true), new CuentaBancaria("9988776655", false)))));
    }

    @Bean
    RepositorioUsuarios repositorioUsuarios(PasswordEncoder codificador) {
        return new RepositorioUsuarios(List.of(
                new Usuario("USR-001", "ana.lopez", codificador.encode("Cliente#2026"), "Cliente", "Ana López", "CLI-001"),
                new Usuario("USR-002", "carlos.perez", codificador.encode("Agente#2026"), "Agente de Atención", "Carlos Pérez", null),
                new Usuario("USR-003", "sofia.ruiz", codificador.encode("Supervisor#2026"), "Supervisor", "Sofía Ruiz", null),
                new Usuario("USR-004", "admin", codificador.encode("Admin#2026"), "Administrador", "Administrador del sistema", null),
                new Usuario("USR-005", "auditor1", codificador.encode("Auditor#2026"), "Auditor", "José Martínez", null)));
    }
}
