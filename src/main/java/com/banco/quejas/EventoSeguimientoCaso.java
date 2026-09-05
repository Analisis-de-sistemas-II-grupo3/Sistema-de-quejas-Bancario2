package com.banco.quejas;

import java.time.LocalDateTime;

/** Evento visible en el historial de seguimiento de CU04. */
public record EventoSeguimientoCaso(LocalDateTime fecha, String descripcion, String estadoAnterior,
                                    String estadoNuevo, String usuario, String rol) { }
