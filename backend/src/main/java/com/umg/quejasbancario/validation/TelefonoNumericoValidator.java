package com.umg.quejasbancario.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TelefonoNumericoValidator implements ConstraintValidator<TelefonoNumerico, String> {

    // RN04: tipo Numerico. Largo maximo = columna telefono_contacto (VARCHAR(20)).
    private static final String PATRON = "^\\d{1,20}$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // Campo opcional (RN04).
        }
        return value.trim().matches(PATRON);
    }
}
