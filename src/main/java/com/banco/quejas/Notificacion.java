package com.banco.quejas;

import java.time.LocalDateTime;

/** Sustituye temporalmente el envío real de correo. */
public record Notificacion(LocalDateTime fecha, String destinatario, String asunto, String mensaje) { }
