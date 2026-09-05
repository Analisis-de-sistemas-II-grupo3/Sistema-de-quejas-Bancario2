package com.banco.quejas;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Identidad visible de la instancia bancaria. Sus valores se parametrizan en
 * application.properties para reutilizar el sistema sin modificar el código.
 */
@Component
public class IdentidadInstitucional {
    @Value("${institucion.nombre:Banco Confianza}")
    private String nombre;
    @Value("${institucion.siglas:BC}")
    private String siglas;
    @Value("${institucion.color-primario:#4A1830}")
    private String colorPrimario;
    @Value("${institucion.color-secundario:#D8A348}")
    private String colorSecundario;
    @Value("${institucion.logo-url:/img/logo-banco.svg}")
    private String logoUrl;

    public String getNombre() { return nombre; }
    public String getSiglas() { return siglas; }
    public String getColorPrimario() { return colorPrimario; }
    public String getColorSecundario() { return colorSecundario; }
    public String getLogoUrl() { return logoUrl; }
}
