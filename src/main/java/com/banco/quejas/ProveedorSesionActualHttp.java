package com.banco.quejas;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

/**
 * Implementación real de CU01 para el puerto {@link ProveedorSesionActual}: lee la
 * identidad autenticada desde la sesión HTTP en lugar del cliente fijo de desarrollo.
 * HttpServletRequest se inyecta como proxy de ámbito de solicitud (comportamiento
 * estándar de Spring Boot Web), por lo que este bean singleton siempre resuelve la
 * solicitud HTTP actual.
 */
@Service
public class ProveedorSesionActualHttp implements ProveedorSesionActual {
    private final HttpServletRequest peticionActual;

    public ProveedorSesionActualHttp(HttpServletRequest peticionActual) {
        this.peticionActual = peticionActual;
    }

    @Override
    public ContextoAutenticacion obtener(String ipCliente) {
        HttpSession sesion = peticionActual.getSession(false);
        if (sesion == null) return null;
        return (ContextoAutenticacion) sesion.getAttribute(ClaveSesion.CONTEXTO);
    }
}
