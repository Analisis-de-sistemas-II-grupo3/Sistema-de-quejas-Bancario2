package com.banco.quejas;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Datos temporales de desarrollo propios de CU02. Catálogos y base de datos los
 * sustituirán. El proveedor de sesión real ahora lo implementa CU01
 * (ver ProveedorSesionActualHttp y ConfiguracionUsuarios).
 */
@Configuration
public class ConfiguracionLocal {
    @Bean
    RegistradorCasos registradorCasos(RepositorioCasosPostgres repositorioPostgres) {
        return new RegistradorCasos(repositorioPostgres);
    }
}
