package com.umg.quejasbancario.dto.request;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class ActualizarUsuarioRequest {
    private String nombreCompleto;

    @Email(message = "El correo electronico ingresado no tiene un formato valido")
    private String correoElectronico;

    private String rol;
}
