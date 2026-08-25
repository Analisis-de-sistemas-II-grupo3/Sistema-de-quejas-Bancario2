package com.banco.quejas;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Expone la identidad institucional en todas las vistas web. */
@ControllerAdvice
public class ConfiguracionVistaGlobal {
    private final IdentidadInstitucional identidad;

    public ConfiguracionVistaGlobal(IdentidadInstitucional identidad) {
        this.identidad = identidad;
    }

    @ModelAttribute("institucion")
    public IdentidadInstitucional identidadInstitucional() {
        return identidad;
    }
}
