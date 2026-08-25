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
            respuesta.sendRedirect(solicitud.getContextPath() + "/login");
            return false;
        }
        return true;
    }
}
