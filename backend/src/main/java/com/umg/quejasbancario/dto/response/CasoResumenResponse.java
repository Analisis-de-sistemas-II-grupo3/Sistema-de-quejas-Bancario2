package com.umg.quejasbancario.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/** Campos resumidos de busqueda/listado, conforme a RN14. */
@Getter
@Builder
@AllArgsConstructor
public class CasoResumenResponse {
    private Integer idCaso;
    private String numeroCaso;
    private LocalDateTime fechaRegistro;
    private String tipoCaso;
    private String nombreCliente;
    private String agenteAsignado;
    private String estado;
}
