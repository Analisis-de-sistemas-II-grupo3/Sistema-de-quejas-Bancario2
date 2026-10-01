package com.umg.quejasbancario.entity.enums;

/**
 * Nombres de rol tal como se cargan en la tabla catalogo ROL (ver RN01
 * y el script de base de datos, seccion "Datos iniciales de catalogo").
 * Se usa para comparaciones y para construir la autoridad de Spring
 * Security con el prefijo ROLE_.
 */
public enum RolNombre {
    CLIENTE("Cliente"),
    AGENTE("Agente"),
    ADMINISTRADOR("Administrador"),
    SUPERVISOR("Supervisor"),
    AUDITOR("Auditor");

    private final String valor;
    RolNombre(String valor) { this.valor = valor; }
    public String getValor() { return valor; }

    public String authority() { return "ROLE_" + name(); }

    public static RolNombre fromValor(String valor) {
        for (RolNombre r : values()) if (r.valor.equalsIgnoreCase(valor)) return r;
        throw new IllegalArgumentException("Rol invalido: " + valor);
    }
}
