package com.umg.quejasbancario.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class DocumentoAdjuntoResponse {
    private Integer idDocumento;
    private String nombreArchivo;
    private String url;
    private Integer tamano;
    private String usuarioCarga;
    private LocalDateTime fechaCarga;
}
