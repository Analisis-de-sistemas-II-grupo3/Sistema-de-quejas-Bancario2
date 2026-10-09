package com.umg.quejasbancario.dto.request;

import com.umg.quejasbancario.validation.DpiNit;
import com.umg.quejasbancario.validation.TelefonoNumerico;
import jakarta.validation.constraints.*;
import lombok.Data;

/** CU-02 Registrar Caso. Cubre RN02/RN03/RN04. */
@Data
public class RegistrarCasoRequest {

    @NotNull(message = "El tipo de caso es obligatorio")
    private Integer idTipoCaso; // Ver RN03

    @NotBlank(message = "El numero de cuenta es obligatorio")
    private String numeroCuenta; // Ver RN16

    @NotBlank(message = "La descripcion del caso es obligatoria")
    @Size(max = 2000, message = "La descripcion no puede superar los 2000 caracteres")
    private String descripcion;

    @NotBlank(message = "El nombre del cliente es obligatorio")
    private String nombreCliente;

    @NotBlank(message = "El DPI/NIT del cliente es obligatorio")
    @DpiNit
    private String identificacionCliente;

    @NotBlank(message = "El correo electronico es obligatorio")
    @Email(message = "Por favor verifique, el correo electrónico ingresado no tiene un formato válido.")
    private String correoContacto;

    @TelefonoNumerico
    private String telefonoContacto; // Opcional, pero si se ingresa debe tener formato válido

    private Integer idProducto; // Opcional (producto/servicio asociado)

    // El documento adjunto (opcional) se sube como multipart aparte, ver
    // CasoController#registrarCaso (multipart/form-data).
}
