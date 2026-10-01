package com.umg.quejasbancario.exception;

/**
 * Excepcion para violaciones de reglas de negocio (RN01-RN16). El mensaje
 * corresponde, cuando aplica, a uno de los mensajes de error definidos en
 * el Anexo AN02 del documento de Reglas de Negocio.
 */
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String mensaje) {
        super(mensaje);
    }
}
