package com.banco.quejas;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Bandejas de destino (paso 7 del flujo básico) para los roles cuyo caso de uso propio
 * aún no existe. Solo confirman el enrutamiento correcto por rol; CU01 no implementa
 * su contenido funcional.
 */
@Controller
public class BandejaController {
    @GetMapping("/bandeja/agente")
    public String bandejaAgente(HttpServletRequest r, Model m) { return bandejaGenerica(r, m, "Casos Asignados"); }

    @GetMapping("/bandeja/supervisor")
    public String bandejaSupervisor(HttpServletRequest r, Model m) { return bandejaGenerica(r, m, "Reasignaciones y Reportes Operativos"); }

    @GetMapping("/admin")
    public String panelAdministrativo(HttpServletRequest r, Model m) { return bandejaGenerica(r, m, "Panel Administrativo"); }

    @GetMapping("/auditoria")
    public String panelAuditoria(HttpServletRequest r, Model m) { return bandejaGenerica(r, m, "Bitácoras y Reportes de Auditoría"); }

    private String bandejaGenerica(HttpServletRequest solicitudHttp, Model modelo, String titulo) {
        HttpSession sesionHttp = solicitudHttp.getSession(false);
        ContextoAutenticacion contexto = sesionHttp == null ? null : (ContextoAutenticacion) sesionHttp.getAttribute(ClaveSesion.CONTEXTO);
        modelo.addAttribute("titulo", titulo);
        modelo.addAttribute("sesion", contexto == null ? null : contexto.sesion());
        return "bandeja-pendiente";
    }
}
