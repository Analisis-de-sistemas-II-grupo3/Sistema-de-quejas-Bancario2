package com.umg.quejasbancario.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SolicitarReasignacionRequest {
    @NotBlank(message = "El motivo de la reasignacion es obligatorio")
    private String motivo;
}
