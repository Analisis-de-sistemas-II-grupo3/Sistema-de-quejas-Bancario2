package com.banco.quejas;

import java.nio.file.Path;

public record DocumentoAdjunto(Path ruta, long tamanioBytes, String extension) { }
