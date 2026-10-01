package com.umg.quejasbancario.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank(message = "El token es obligatorio")
    private String token;

    // Minimo 8 caracteres, al menos una letra y un numero (RNF: requisitos de seguridad)
    @NotBlank(message = "La nueva contrasena es obligatoria")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$",
             message = "La contrasena no cumple con los requisitos de seguridad")
    private String nuevaContrasena;
}
