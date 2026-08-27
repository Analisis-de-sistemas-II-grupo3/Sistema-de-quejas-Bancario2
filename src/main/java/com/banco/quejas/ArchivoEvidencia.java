package com.banco.quejas;

/** Contenido de una evidencia listo para enviarse al navegador. */
public record ArchivoEvidencia(String nombre, String tipoContenido, byte[] contenido) { }
