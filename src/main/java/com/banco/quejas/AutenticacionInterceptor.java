package com.banco.quejas;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/** Exige una sesión de CU01 activa antes de acceder a cualquier bandeja protegida. */
@Component
public class AutenticacionInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest solicitud, HttpServletResponse respuesta, Object manejador) throws Exception {
        HttpSession sesion = solicitud.getSession(false);
        if (sesion == null || sesion.getAttribute(ClaveSesion.CONTEXTO) == null) {
            // Si la sesión caducó, CU01 indica regresar al portal. Desde allí el
            // usuario puede volver a iniciar sesión sin quedar en una pantalla rota.
            respuesta.sendRedirect(solicitud.getContextPath() + "/?sesionExpirada=true");
            return false;
        }
        return true;
    }
}
