package com.umg.quejasbancario.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** Usado para Categoria y ProductoServicio: solo tienen campo "nombre". */
@Data
public class CatalogoSimpleRequest {
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
}
