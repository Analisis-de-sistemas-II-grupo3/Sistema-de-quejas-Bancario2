package com.banco.quejas;

import java.util.List;
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
    RegistradorCasos registradorCasos() {
        return new RegistradorCasos(List.of(
                new ProductoServicio("CTA", "Cuenta de ahorro"),
                new ProductoServicio("TAR", "Tarjeta de crédito"),
                new ProductoServicio("BAN", "Banca en línea")),
                List.of(new AgenteAtencion("AG-01", "Carlos Pérez", true, 2, 10),
                        new AgenteAtencion("AG-02", "María Gómez", true, 10, 10)));
    }
}
