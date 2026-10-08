package com.umg.quejasbancario.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Vista publica (sin sesion) del estado de un caso (CU-00 FA01 / CU-03).
 * Expone unicamente lo necesario para dar seguimiento: NO incluye datos del
 * cliente, descripcion, agente asignado, resolucion, documentos ni IPs.
 */
@Getter
@Builder
@AllArgsConstructor
public class CasoPublicoResponse {
    private String numeroCaso;
    private String tipoCaso;
    private String estado;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaUltimaActualizacion;
    private LocalDateTime fechaCierre;
    private List<Etapa> etapas;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Etapa {
        private String estadoAnterior;
        private String estadoNuevo;
        private LocalDateTime fechaHora;
    }
}
