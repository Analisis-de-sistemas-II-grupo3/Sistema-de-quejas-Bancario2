package com.umg.quejasbancario.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class BitacoraAccesoResponse {
    private Long id;
    private String usuario;
    private String ip;
    private String tipoEvento;
    private String descripcionEvento;
    private LocalDateTime fechaHora;
}
