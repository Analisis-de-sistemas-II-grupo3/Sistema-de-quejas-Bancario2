package com.umg.quejasbancario.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** CU-14 Gestionar Usuarios - Agregar Usuario. */
@Data
public class CrearUsuarioRequest {
    @NotBlank(message = "El nombre de usuario es obligatorio")
    private String nombreUsuario;

    @NotBlank(message = "El nombre completo es obligatorio")
    private String nombreCompleto;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo electronico ingresado no tiene un formato valido")
    private String correoElectronico;

    @NotBlank(message = "El rol es obligatorio")
    private String rol; // Cliente, Agente, Administrador, Supervisor, Auditor (RN01)

    /** Opcional: si no se envia, se genera una contrasena temporal y se notifica por correo. */
    private String contrasenaInicial;
}
