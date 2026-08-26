package com.banco.quejas;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** CU00 - Portal público y punto de entrada al sistema. */
@Controller
public class PortalController {
    @GetMapping("/")
    public String mostrarInicio() {
        return "inicio";
    }

    /** Enlace público preparado para el CU de consulta de estado. */
    @GetMapping("/consulta-estado")
    public String consultaEstadoPendiente() {
        return "consulta-estado-pendiente";
    }
}
