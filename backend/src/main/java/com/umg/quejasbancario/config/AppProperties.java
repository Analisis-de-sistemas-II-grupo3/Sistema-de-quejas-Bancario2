package com.umg.quejasbancario.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

/**
 * Agrupa parametros de configuracion propios de la aplicacion que no
 * pertenecen a Spring en si: seguridad de acceso (RNF02, bloqueo por
 * intentos fallidos), correo saliente (RN09), url del frontend para
 * enlaces de recuperacion de contrasena, y directorio de subida de
 * documentos adjuntos (RN10).
 */
@Configuration
@ConfigurationProperties(prefix = "app")
@Data
public class AppProperties {
    private final Mail mail = new Mail();
    private final Frontend frontend = new Frontend();
    private final Seguridad seguridad = new Seguridad();
    private final Uploads uploads = new Uploads();
    private final Cors cors = new Cors();

    @Data
    public static class Mail {
        private String from;
        private String fromName;
    }

    @Data
    public static class Frontend {
        private String resetPasswordUrl;
    }

    @Data
    public static class Seguridad {
        private int maxIntentosFallidos;
        private int bloqueoMinutos;
        private int resetTokenMinutos;
    }

    @Data
    public static class Uploads {
        private String dir;
    }

    @Data
    public static class Cors {
        private String allowedOrigins;
    }
}
