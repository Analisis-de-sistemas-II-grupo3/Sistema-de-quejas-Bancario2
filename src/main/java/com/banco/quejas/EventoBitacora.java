package com.banco.quejas;

import java.time.LocalDateTime;

public record EventoBitacora(LocalDateTime fecha, String numeroCaso, String evento,
                             String estadoAnterior, String estadoNuevo, String rolEjecutor,
                             String usuarioEjecutor, String ip, String descripcion) { }
