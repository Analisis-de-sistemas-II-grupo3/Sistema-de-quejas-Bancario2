package com.umg.quejasbancario.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ParametroRequest {
    @NotBlank(message = "El valor es obligatorio")
    private String valor;
}
