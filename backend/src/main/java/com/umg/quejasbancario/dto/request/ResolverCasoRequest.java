package com.umg.quejasbancario.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** CU-08 Resolver Caso. */
@Data
public class ResolverCasoRequest {
    @NotBlank(message = "El detalle de la resolucion es obligatorio")
    private String detalleResolucion;
}
