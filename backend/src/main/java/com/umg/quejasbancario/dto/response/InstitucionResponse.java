package com.umg.quejasbancario.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/** Identidad institucional publica del banco (RN17, CU-00). */
@Getter
@Builder
@AllArgsConstructor
public class InstitucionResponse {
    private String nombre;
    private String eslogan;
    private String logoUrl;
    private Colores colores;
    private String mision;
    private String vision;
    private List<Valor> valores;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Colores {
        private String primario;
        private String secundario;
        private String acento;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Valor {
        private String nombre;
        private String descripcion;
    }
}
