package com.umg.quejasbancario.entity.enums;

public enum TipoEventoAcceso {
    INICIO_SESION("Inicio de sesión"),
    CIERRE_SESION("Cierre de sesión");

    private final String valor;
    TipoEventoAcceso(String valor) { this.valor = valor; }
    public String getValor() { return valor; }

    public static TipoEventoAcceso fromValor(String valor) {
        for (TipoEventoAcceso e : values()) if (e.valor.equalsIgnoreCase(valor)) return e;
        throw new IllegalArgumentException("Tipo de evento de acceso invalido: " + valor);
    }
}
