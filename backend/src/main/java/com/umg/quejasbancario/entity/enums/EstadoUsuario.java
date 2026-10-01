package com.umg.quejasbancario.entity.enums;

public enum EstadoUsuario {
    ACTIVO("Activo"),
    INACTIVO("Inactivo");

    private final String valor;
    EstadoUsuario(String valor) { this.valor = valor; }
    public String getValor() { return valor; }

    public static EstadoUsuario fromValor(String valor) {
        for (EstadoUsuario e : values()) if (e.valor.equalsIgnoreCase(valor)) return e;
        throw new IllegalArgumentException("Estado de usuario invalido: " + valor);
    }
}
