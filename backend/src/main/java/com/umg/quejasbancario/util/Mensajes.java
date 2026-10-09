package com.umg.quejasbancario.util;

import com.umg.quejasbancario.exception.BusinessRuleException;
import jakarta.servlet.http.HttpServletResponse;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Catalogo CENTRAL de mensajes del documento de Reglas de Negocio:
 *   - AN01: Mensajes de Respuesta - Exito
 *   - AN02: Mensajes de Respuesta - Error
 * Todo el backend debe tomar los textos de aqui para que la pantalla muestre
 * exactamente lo que definen las reglas de negocio y los casos de uso.
 *
 * Los mensajes de exito (AN01) viajan al frontend en la cabecera HTTP
 * "X-Mensaje" (URL-encoded, separados por coma); el frontend (api.js) los
 * muestra automaticamente como notificacion. Los de error (AN02) viajan en el
 * campo "mensaje" del cuerpo de ErrorResponse.
 */
public final class Mensajes {

    private Mensajes() {}

    public static final String HEADER = "X-Mensaje";

    // =====================================================================
    // AN01 - Mensajes de exito
    // =====================================================================

    /** AN01 #1 */
    public static String casoRegistrado(String numeroCaso) {
        return "El caso se registró con éxito. Número de caso: " + numeroCaso + ".";
    }

    /** AN01 #2 */
    public static String casoAsignado(String nombreAgente) {
        return "El caso fue asignado automáticamente al agente " + nombreAgente + ".";
    }

    /** AN01 #3 */
    public static String estadoActualizado(String numeroCaso, String estadoAnterior, String estadoNuevo) {
        return "Se actualizó el estado del caso " + numeroCaso + " de " + estadoAnterior + " a " + estadoNuevo + ".";
    }

    /** AN01 #4 */
    public static String resolucionRegistrada(String numeroCaso) {
        return "Se registró la resolución del caso " + numeroCaso + " con éxito.";
    }

    /** AN01 #5 */
    public static String casoCerrado(String numeroCaso) {
        return "El caso " + numeroCaso + " fue cerrado con éxito.";
    }

    /** AN01 #6 */
    public static String reasignacionSolicitada(String numeroCaso) {
        return "Se registró la solicitud de reasignación del caso " + numeroCaso + ".";
    }

    /** AN01 #7 */
    public static String reasignacionAprobada(String numeroCaso, String nombreAgente) {
        return "Se aprobó la reasignación del caso " + numeroCaso + ". Nuevo agente: " + nombreAgente + ".";
    }

    /** AN01 #14 */
    public static String reasignacionRechazada(String numeroCaso) {
        return "Se rechazó la solicitud de reasignación del caso " + numeroCaso + ".";
    }

    public static final String AN01_08_USUARIO_AGREGADO = "Se agregó el nuevo usuario con éxito.";
    public static final String AN01_09_CATALOGO_ACTUALIZADO = "Se actualizó el catálogo con éxito.";
    public static final String AN01_10_INICIO_SESION = "Inicio de sesión exitoso.";
    public static final String AN01_11_CIERRE_SESION = "Cierre de sesión exitoso.";
    public static final String AN01_12_ENLACE_ENVIADO = "Se envió un enlace de recuperación de contraseña a su correo electrónico.";
    public static final String AN01_13_CONTRASENA_ACTUALIZADA = "Contraseña actualizada exitosamente.";
    public static final String AN01_15_PARAMETRO_ACTUALIZADO = "Se actualizó el parámetro del sistema con éxito.";
    public static final String AN01_16_USUARIO_ACTUALIZADO = "Se actualizó la información del usuario con éxito";

    // =====================================================================
    // AN02 - Mensajes de error
    // =====================================================================

    public static final String AN02_01 = "Por favor ingrese los campos obligatorios.";
    public static final String AN02_02 = "Por favor cargue un documento en formato PDF o imagen.";
    public static final String AN02_03 = "Verifique el tamaño del documento, el máximo permitido es 10MB.";
    public static final String AN02_04 = "Por favor verifique, no existen agentes disponibles en este momento.";
    public static final String AN02_05 = "Por favor verifique, el número de caso no existe en el sistema.";
    public static final String AN02_06 = "No es posible realizar esta acción porque el caso se encuentra en estado 'Cerrado'.";
    public static final String AN02_07 = "Por favor verifique, el correo electrónico ingresado no tiene un formato válido.";
    public static final String AN02_08 = "Por favor verifique, ya existe una solicitud de reasignación pendiente para este caso.";
    public static final String AN02_09 = "Por favor verifique, el número de cuenta ingresado no corresponde a una cuenta activa. Solo clientes con cuenta activa pueden registrar casos.";
    public static final String AN02_10 = "Por favor verifique, el documento adjunto supera el tamaño máximo permitido de 2MB.";
    public static final String AN02_11 = "Usuario o contraseña incorrectos.";
    public static final String AN02_12 = "Su cuenta ha sido bloqueada temporalmente por múltiples intentos fallidos de inicio de sesión.";
    public static final String AN02_13 = "Su usuario se encuentra inactivo. Contacte al Administrador.";
    public static final String AN02_14 = "El correo ingresado no corresponde a ningún usuario registrado.";
    public static final String AN02_15 = "El enlace de recuperación ha expirado. Por favor solicite uno nuevo.";
    public static final String AN02_16 = "La contraseña no cumple con los requisitos de seguridad.";
    public static final String AN02_17 = "No tiene autorización para consultar este caso.";
    public static final String AN02_18 = "No cuenta con casos registrados.";
    public static final String AN02_19 = "Se alcanzó el número máximo de solicitudes de reasignación permitidas para este caso.";
    public static final String AN02_20 = "No existen solicitudes de reasignación pendientes de aprobación.";
    public static final String AN02_21 = "La acción solicitada no es válida para el estado actual del caso.";
    public static final String AN02_22 = "No se encontraron casos con los criterios de búsqueda indicados.";
    public static final String AN02_23 = "La fecha de inicio no puede ser mayor a la fecha de fin.";
    public static final String AN02_24 = "No es posible inactivar al usuario porque tiene casos activos sin reasignar.";
    public static final String AN02_25 = "El nombre de usuario ingresado ya existe en el sistema.";
    public static final String AN02_26 = "El valor de catálogo ingresado ya existe.";
    public static final String AN02_27 = "El valor ingresado debe ser un número mayor a cero.";

    // =====================================================================
    // Utilidades
    // =====================================================================

    /** AN02 #23: valida que el rango de fechas sea coherente. */
    public static void validarRangoFechas(LocalDateTime desde, LocalDateTime hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new BusinessRuleException(AN02_23);
        }
    }

    /**
     * Agrega uno o varios mensajes AN01 a la respuesta HTTP actual (cabecera
     * X-Mensaje). Debe invocarse antes de que el controller retorne el cuerpo.
     */
    public static void enviar(HttpServletResponse respuesta, String... mensajes) {
        String valor = Arrays.stream(mensajes)
                .filter(Objects::nonNull)
                .filter(m -> !m.isBlank())
                .map(m -> URLEncoder.encode(m, StandardCharsets.UTF_8).replace("+", "%20"))
                .collect(Collectors.joining(","));
        if (!valor.isEmpty()) {
            respuesta.setHeader(HEADER, valor);
        }
    }
}
