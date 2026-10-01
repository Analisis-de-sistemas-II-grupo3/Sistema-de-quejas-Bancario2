package com.umg.quejasbancario.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CuentaBancariaResponse {
    private Integer idCuenta;
    private String numeroCuenta;
    private String estado;
}
