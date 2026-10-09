package com.umg.quejasbancario.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Valida el FORMATO de un DPI (CUI) o NIT guatemalteco. No verifica que el
 * documento sea real ni que pertenezca a la persona (eso requeriria una
 * consulta contra el RENAP/SAT, fuera del alcance de este proyecto) - solo
 * valida que la cantidad y el tipo de caracteres sean validos:
 *   - DPI/CUI: exactamente 13 digitos (con o sin espacios: "1234 56789 0123").
 *   - NIT: de 2 a 9 caracteres, digitos y opcionalmente terminado en una
 *     letra 'K' (digito verificador), con o sin guion (ej. "12345678-9",
 *     "1234567-K"). Aplica tanto a personas individuales como a empresas.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DpiNitValidator.class)
public @interface DpiNit {
    String message() default "Por favor ingrese los campos obligatorios.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
