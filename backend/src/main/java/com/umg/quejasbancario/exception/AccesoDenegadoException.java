package com.umg.quejasbancario.exception;

/** Ver AN02 #17: "No tiene autorizacion para consultar este caso." */
public class AccesoDenegadoException extends RuntimeException {
    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
