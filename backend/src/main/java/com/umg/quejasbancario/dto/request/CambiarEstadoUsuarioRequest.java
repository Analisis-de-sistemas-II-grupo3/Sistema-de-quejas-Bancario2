package com.umg.quejasbancario.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CambiarEstadoUsuarioRequest {
    @NotBlank(message = "El estado es obligatorio")
    private String estado; // Activo | Inactivo (RN13)
}
