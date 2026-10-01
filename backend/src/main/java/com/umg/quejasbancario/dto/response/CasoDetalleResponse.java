package com.umg.quejasbancario.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class CasoDetalleResponse {
    private Integer idCaso;
    private String numeroCaso;
    private String tipoCaso;
    private String categoria;
    private String producto;
    private String descripcion;
    private String estado;
    private String numeroCuenta;
    private String nombreCliente;
    private String identificacionCliente;
    private String correoContacto;
    private String telefonoContacto;
    private String agenteAsignado;
    private String detalleResolucion;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaCierre;
    private Integer solicitudesReasignacionUsadas;
    private List<DocumentoAdjuntoResponse> documentos;
    private List<HistorialCasoResponse> historial;
}
