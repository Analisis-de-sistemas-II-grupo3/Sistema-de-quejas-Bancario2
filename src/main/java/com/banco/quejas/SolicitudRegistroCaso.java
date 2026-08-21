package com.banco.quejas;

import java.time.LocalDate;

/** Datos propios del formulario del CU02 V1.4. La identidad se obtiene del CU01. */
public record SolicitudRegistroCaso(TipoCaso tipo, String productoId, String sucursalCanal,
                                    LocalDate fechaHecho, String descripcion,
                                    DocumentoAdjunto adjunto) { }
