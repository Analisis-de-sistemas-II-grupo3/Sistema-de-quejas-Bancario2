package com.banco.quejas;

import java.util.Map;

/**
 * Traduce el rol de sesión a su bandeja principal (flujo básico, paso 7).
 * Cliente/Denunciante ya tiene bandeja real (CU04); las demás son marcadores
 * de posición hasta que su caso de uso correspondiente las implemente.
 */
public final class RutaBandeja {
    private static final Map<String, String> RUTAS = Map.of(
            "Cliente", "/consulta-estado",
            "Agente de Atención", "/bandeja/agente",
            "Supervisor", "/bandeja/supervisor",
            "Administrador", "/admin",
            "Auditor", "/auditoria");

    private RutaBandeja() { }

    public static String paraRol(String rol) {
        return RUTAS.getOrDefault(rol, "/login");
    }
}