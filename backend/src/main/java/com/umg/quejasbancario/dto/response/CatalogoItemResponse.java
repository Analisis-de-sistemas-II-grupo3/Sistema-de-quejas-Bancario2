package com.umg.quejasbancario.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CatalogoItemResponse {
    private Integer id;
    private String nombre;
    private String extra; // ej. prefijo en TipoCaso
}
