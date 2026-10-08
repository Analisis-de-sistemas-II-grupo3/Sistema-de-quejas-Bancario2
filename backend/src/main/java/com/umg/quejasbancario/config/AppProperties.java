package com.umg.quejasbancario.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Agrupa parametros de configuracion propios de la aplicacion que no
 * pertenecen a Spring en si: seguridad de acceso (RNF02, bloqueo por
 * intentos fallidos), correo saliente (RN09), url del frontend para
 * enlaces de recuperacion de contrasena, y directorio de subida de
 * documentos adjuntos (RN10) e identidad institucional configurable
 * del banco (RN17, CU-00).
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
    private final Institucion institucion = new Institucion();
    private final RateLimit rateLimit = new RateLimit();

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

    /**
     * Identidad institucional (RN17): se carga siempre desde la
     * configuracion de la instancia desplegada, nunca desde el codigo.
     * Cada instancia sirve a un unico banco.
     */
    @Data
    public static class Institucion {
        private String nombre;
        private String eslogan;
        /** URL del logotipo; si es relativa se resuelve contra el sitio del frontend. */
        private String logoUrl;
        private String colorPrimario;
        private String colorSecundario;
        private String colorAcento;
        private String mision;
        private String vision;
        private List<Valor> valores = new ArrayList<>();
    }

    @Data
    public static class Valor {
        private String nombre;
        private String descripcion;
    }

    /** Limite de consultas por IP a los endpoints publicos de casos (/api/publico/casos/**). */
    @Data
    public static class RateLimit {
        private int maxPeticiones = 10;
        private int ventanaSegundos = 60;
    }
}
