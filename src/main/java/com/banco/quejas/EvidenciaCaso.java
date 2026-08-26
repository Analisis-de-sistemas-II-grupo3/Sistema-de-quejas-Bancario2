package com.banco.quejas;

import java.nio.file.Path;
import java.time.LocalDateTime;

/** Archivo validado y asociado a un caso. */
public record EvidenciaCaso(Long id, String nombreOriginal, Path rutaGuardada, long tamanioBytes,
                            String extension, String usuarioQueCargo, LocalDateTime fechaCarga) { }
