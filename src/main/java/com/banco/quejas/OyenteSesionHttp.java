package com.banco.quejas;

import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.springframework.stereotype.Component;

/**
 * FA03: cuando el contenedor expira una sesión por inactividad (ver
 * server.servlet.session.timeout), este listener la detecta y registra la bitácora
 * del cierre automático. Si el cierre fue manual (FA02), LoginController ya marcó
 * ClaveSesion.CIERRE_MANUAL antes de invalidar, y aquí se omite el registro duplicado.
 */
@Component
public class OyenteSesionHttp implements HttpSessionListener {
    private final BitacoraAutenticacion bitacora;

    public OyenteSesionHttp(BitacoraAutenticacion bitacora) {
        this.bitacora = bitacora;
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent evento) {
        var sesion = evento.getSession();
        boolean cierreManual = Boolean.TRUE.equals(sesion.getAttribute(ClaveSesion.CIERRE_MANUAL));
        if (cierreManual || !(sesion.getAttribute(ClaveSesion.CONTEXTO) instanceof ContextoAutenticacion contexto)) {
            return;
        }
        bitacora.registrar("CIERRE AUTOMÁTICO POR INACTIVIDAD", contexto.sesion().usuario(), contexto.sesion().rol(),
                contexto.sesion().ip(), "La sesión se cerró automáticamente por inactividad.");
    }
}
