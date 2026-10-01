package com.umg.quejasbancario.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class UsuarioResponse {
    private Integer idUsuario;
    private String nombreUsuario;
    private String nombreCompleto;
    private String correoElectronico;
    private String rol;
    private String estado;
    private LocalDateTime fechaCreacion;
}
