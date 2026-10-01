package com.umg.quejasbancario.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class BitacoraUsuarioResponse {
    private Long id;
    private String usuarioAfectado;
    private String usuarioEjecuta;
    private String motivo;
    private String descripcionEvento;
    private LocalDateTime fechaHora;
}
