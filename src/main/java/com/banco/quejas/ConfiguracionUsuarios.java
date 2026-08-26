package com.banco.quejas;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
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
    RepositorioClientes repositorioClientes(JdbcTemplate jdbc) {
        return new RepositorioClientes(jdbc);
    }

    @Bean
    RepositorioUsuarios repositorioUsuarios(JdbcTemplate jdbc) {
        return new RepositorioUsuarios(jdbc);
    }
}
