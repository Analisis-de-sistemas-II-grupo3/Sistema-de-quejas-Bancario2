package com.banco.quejas;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/** Controlador web del CU01 V1.3 - Iniciar Sesión. */
@Controller
public class LoginController {
    private final ServicioAutenticacion servicioAutenticacion;
    private final BitacoraAutenticacion bitacora;

    public LoginController(ServicioAutenticacion servicioAutenticacion, BitacoraAutenticacion bitacora) {
        this.servicioAutenticacion = servicioAutenticacion;
        this.bitacora = bitacora;
    }

    @GetMapping("/login")
    public String mostrarFormulario(HttpServletRequest solicitudHttp, Model modelo) {
        HttpSession sesionExistente = solicitudHttp.getSession(false);
        if (sesionExistente != null && sesionExistente.getAttribute(ClaveSesion.CONTEXTO) instanceof ContextoAutenticacion contexto) {
            return "redirect:" + RutaBandeja.paraRol(contexto.sesion().rol());
        }
        modelo.addAttribute("credenciales", new CredencialesLogin());
        return "login";
    }

    /** Flujo básico pasos 4-7. FA01: credenciales inválidas regresa al paso 2. */
    @PostMapping("/login")
    public String iniciarSesion(@ModelAttribute CredencialesLogin credenciales, HttpServletRequest solicitudHttp, Model modelo) {
        try {
            ContextoAutenticacion contexto = servicioAutenticacion.autenticar(
                    credenciales.getUsuario(), credenciales.getContrasena(), solicitudHttp.getRemoteAddr());
            HttpSession sesion = solicitudHttp.getSession(true);
            sesion.setAttribute(ClaveSesion.CONTEXTO, contexto);
            return "redirect:" + RutaBandeja.paraRol(contexto.sesion().rol());
        } catch (AutenticacionException e) {
            modelo.addAttribute("credenciales", new CredencialesLogin());
            modelo.addAttribute("error", e.getMessage());
            return "login";
        }
    }

    /** FA02: cierre de sesión manual. */
    @GetMapping("/logout")
    public String cerrarSesion(HttpServletRequest solicitudHttp) {
        HttpSession sesion = solicitudHttp.getSession(false);
        if (sesion != null) {
            if (sesion.getAttribute(ClaveSesion.CONTEXTO) instanceof ContextoAutenticacion contexto) {
                bitacora.registrar("CIERRE DE SESIÓN", contexto.sesion().usuario(), contexto.sesion().rol(),
                        contexto.sesion().ip(), "El usuario cerró sesión manualmente.");
            }
            sesion.setAttribute(ClaveSesion.CIERRE_MANUAL, Boolean.TRUE);
            sesion.invalidate();
        }
        return "redirect:/login";
    }
}
