package com.banco.quejas;

/** Convierte el nombre técnico de PostgreSQL al nombre que usa la aplicación web. */
public final class RolesAplicacion {
    private RolesAplicacion() { }
    public static String convertir(String rolBase) {
        if (rolBase == null) return "";
        return switch (rolBase.toUpperCase()) {
            case "CLIENTE" -> "Cliente";
            case "AGENTE" -> "Agente de Atención";
            case "SUPERVISOR" -> "Supervisor";
            case "ADMINISTRADOR" -> "Administrador";
            case "AUDITOR" -> "Auditor";
            default -> rolBase;
        };
    }
}
