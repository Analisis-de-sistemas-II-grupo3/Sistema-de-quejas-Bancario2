package com.banco.quejas;

import java.time.LocalDateTime;

/**
 * Evento de bitácora de inicio/cierre de sesión (CU01). Se mantiene separado de
 * {@link EventoBitacora} de CU02 porque no está ligado a un caso. CU Registrar Bitácora
 * de Auditoría unificará y persistirá ambos.
 */
public record EventoBitacoraAcceso(LocalDateTime fecha, String evento, String usuario, String rol,
                                   String ip, String descripcion) { }
