package com.umg.quejasbancario.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordRequest {
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo electronico ingresado no tiene un formato valido")
    private String correoElectronico;
}
