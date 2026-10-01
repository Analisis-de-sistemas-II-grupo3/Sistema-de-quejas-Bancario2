package com.umg.quejasbancario.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TipoCasoRequest {
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El prefijo es obligatorio")
    @Size(min = 1, max = 1, message = "El prefijo debe ser un solo caracter")
    private String prefijo;
}
