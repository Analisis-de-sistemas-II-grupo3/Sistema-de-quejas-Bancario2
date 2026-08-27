package com.banco.quejas;

/** Error que se muestra cuando CU03 no puede adjuntar una evidencia. */
public class ErrorEvidenciaException extends RuntimeException {
    public ErrorEvidenciaException(String mensaje) { super(mensaje); }
}
