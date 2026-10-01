package com.umg.quejasbancario.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MessageResponse {
    private String mensaje;

    public static MessageResponse of(String mensaje) {
        return new MessageResponse(mensaje);
    }
}
