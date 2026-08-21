package com.banco.quejas;

public record AgenteAtencion(String id, String nombre, boolean sesionActiva,
                             int casosActivos, int limiteCasosActivos) {
    public boolean disponible() {
        return sesionActiva && casosActivos < limiteCasosActivos;
    }
}
