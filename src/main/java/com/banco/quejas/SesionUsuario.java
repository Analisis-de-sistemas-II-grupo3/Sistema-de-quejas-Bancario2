package com.banco.quejas;

/** Datos auditables de la sesión que está registrando el caso. */
public record SesionUsuario(String usuario, String rol, String ip) {
    public SesionUsuario {
        if (usuario == null || usuario.isBlank()) throw new IllegalArgumentException("El usuario de sesión es obligatorio.");
        if (rol == null || rol.isBlank()) throw new IllegalArgumentException("El rol de sesión es obligatorio.");
        if (ip == null || ip.isBlank()) throw new IllegalArgumentException("La IP de sesión es obligatoria.");
    }
}
