package com.umg.quejasbancario.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String tipo; // "Bearer"
    private Integer idUsuario;
    private String nombreUsuario;
    private String nombreCompleto;
    private String rol;
    private long expiraEnMs;
}
