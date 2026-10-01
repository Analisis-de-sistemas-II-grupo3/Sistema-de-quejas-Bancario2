package com.umg.quejasbancario.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class BitacoraCorreoResponse {
    private Long id;
    private String numeroCaso;
    private String destinatario;
    private String tipoNotificacion;
    private String descripcionEvento;
    private LocalDateTime fechaHora;
}
