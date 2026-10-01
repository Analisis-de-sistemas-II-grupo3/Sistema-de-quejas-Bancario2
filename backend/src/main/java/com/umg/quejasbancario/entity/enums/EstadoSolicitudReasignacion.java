package com.umg.quejasbancario.entity.enums;

public enum EstadoSolicitudReasignacion {
    PENDIENTE("Pendiente"),
    APROBADA("Aprobada"),
    RECHAZADA("Rechazada");

    private final String valor;
    EstadoSolicitudReasignacion(String valor) { this.valor = valor; }
    public String getValor() { return valor; }

    public static EstadoSolicitudReasignacion fromValor(String valor) {
        for (EstadoSolicitudReasignacion e : values()) if (e.valor.equalsIgnoreCase(valor)) return e;
        throw new IllegalArgumentException("Estado de solicitud invalido: " + valor);
    }
}
