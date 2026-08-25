package com.banco.quejas;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Caso creado por CU02 y vinculado obligatoriamente al Cliente autenticado. */
public record Caso(String folio, String clienteId, String clienteNombre, TipoCaso tipo,
                   ProductoServicio producto, String sucursalCanal, LocalDate fechaHecho,
                   String descripcion, DocumentoAdjunto adjunto, EstadoCaso estado,
                   AgenteAtencion agenteAsignado, LocalDateTime fechaRegistro) { }
