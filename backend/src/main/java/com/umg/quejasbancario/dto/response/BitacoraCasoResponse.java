package com.umg.quejasbancario.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class BitacoraCasoResponse {
    private Long id;
    private String numeroCaso;
    private String rolEjecuta;
    private String usuario;
    private String ip;
    private String estadoAnterior;
    private String estadoNuevo;
    private String descripcionEvento;
    private LocalDateTime fechaHora;
}
