package com.umg.quejasbancario.entity.enums;

/**
 * Ciclo de vida del caso segun RN08.
 * Registrado -> Asignado -> En atencion -> (En espera <-> En atencion) -> Resuelto -> Cerrado
 */
public enum EstadoCaso {
    REGISTRADO("Registrado"),
    ASIGNADO("Asignado"),
    EN_ATENCION("En atención"),
    EN_ESPERA("En espera"),
    RESUELTO("Resuelto"),
    CERRADO("Cerrado");

    private final String valor;
    EstadoCaso(String valor) { this.valor = valor; }
    public String getValor() { return valor; }

    public static EstadoCaso fromValor(String valor) {
        for (EstadoCaso e : values()) if (e.valor.equalsIgnoreCase(valor)) return e;
        throw new IllegalArgumentException("Estado de caso invalido: " + valor);
    }
}
