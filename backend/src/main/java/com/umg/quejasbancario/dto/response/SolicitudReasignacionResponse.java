package com.umg.quejasbancario.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class SolicitudReasignacionResponse {
    private Integer idSolicitud;
    private String numeroCaso;
    private String agenteSolicita;
    private String supervisorResuelve;
    private String motivo;
    private String motivoRechazo;
    private String estado;
    /** Agente que tiene el caso en este momento (tras aprobar: el nuevo agente). */
    private String agenteActual;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaResolucion;
}
