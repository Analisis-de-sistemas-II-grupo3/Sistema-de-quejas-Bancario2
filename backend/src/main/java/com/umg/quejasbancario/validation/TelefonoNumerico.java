package com.umg.quejasbancario.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * RN04 - Telefono de Contacto: dato de tipo Numerico y opcional.
 * Acepta unicamente digitos (maximo 20, el largo de la columna). Si viene
 * vacio, esta validacion no aplica. El mensaje es el AN02 #1.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TelefonoNumericoValidator.class)
public @interface TelefonoNumerico {
    String message() default "Por favor ingrese los campos obligatorios.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
