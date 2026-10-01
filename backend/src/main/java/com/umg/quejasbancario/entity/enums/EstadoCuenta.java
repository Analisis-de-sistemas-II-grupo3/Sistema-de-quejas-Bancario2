package com.umg.quejasbancario.entity.enums;

public enum EstadoCuenta {
    ACTIVA("Activa"),
    INACTIVA("Inactiva");

    private final String valor;
    EstadoCuenta(String valor) { this.valor = valor; }
    public String getValor() { return valor; }

    public static EstadoCuenta fromValor(String valor) {
        for (EstadoCuenta e : values()) if (e.valor.equalsIgnoreCase(valor)) return e;
        throw new IllegalArgumentException("Estado de cuenta invalido: " + valor);
    }
}
