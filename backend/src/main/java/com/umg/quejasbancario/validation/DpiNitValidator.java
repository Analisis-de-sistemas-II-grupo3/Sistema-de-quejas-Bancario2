package com.umg.quejasbancario.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DpiNitValidator implements ConstraintValidator<DpiNit, String> {

    // DPI/CUI: 13 digitos exactos.
    private static final String PATRON_DPI = "^\\d{13}$";

    // NIT: 1 a 8 digitos + 1 digito verificador (0-9 o K), ej. 12345678-9 o 1234567-K.
    // Cubre tanto NIT de personas como de empresas (ambos usan este mismo esquema en Guatemala).
    private static final String PATRON_NIT = "^\\d{1,8}[0-9Kk]$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // La obligatoriedad la controla @NotBlank por separado.
        }
        String limpio = value.replaceAll("[\\s-]", "");
        return limpio.matches(PATRON_DPI) || limpio.matches(PATRON_NIT);
    }
}
