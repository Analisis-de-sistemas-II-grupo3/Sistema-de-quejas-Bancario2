package com.umg.quejasbancario.util;

import jakarta.servlet.http.HttpServletRequest;

/** Extrae la IP real del cliente (RN15: toda accion auditable registra la IP). */
public class IpUtil {

    private IpUtil() {}

    public static String obtenerIp(HttpServletRequest request) {
        if (request == null) return "N/D";
        String header = request.getHeader("X-Forwarded-For");
        if (header != null && !header.isBlank()) {
            return header.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp;
        }
        return request.getRemoteAddr();
    }
}
