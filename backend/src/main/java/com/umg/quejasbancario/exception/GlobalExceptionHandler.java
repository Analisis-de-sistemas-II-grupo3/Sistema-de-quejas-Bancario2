package com.umg.quejasbancario.exception;

import com.umg.quejasbancario.util.Mensajes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Traduce toda excepcion a un ErrorResponse cuyo "mensaje" es el texto del
 * Anexo AN02 que corresponde (ver util.Mensajes). El frontend muestra ese
 * "mensaje" tal cual en pantalla.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRule(BusinessRuleException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    @ExceptionHandler(AccesoDenegadoException.class)
    public ResponseEntity<ErrorResponse> handleAccesoDenegado(AccesoDenegadoException ex) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), null);
    }

    @ExceptionHandler(CuentaBloqueadaException.class)
    public ResponseEntity<ErrorResponse> handleCuentaBloqueada(CuentaBloqueadaException ex) {
        return build(HttpStatus.LOCKED, ex.getMessage(), null);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        return build(HttpStatus.UNAUTHORIZED, Mensajes.AN02_11, null);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ErrorResponse> handleLocked(LockedException ex) {
        return build(HttpStatus.LOCKED, Mensajes.AN02_12, null);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> handleDisabled(DisabledException ex) {
        return build(HttpStatus.FORBIDDEN, Mensajes.AN02_13, null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleSpringAccessDenied(AccessDeniedException ex) {
        return build(HttpStatus.FORBIDDEN, "No tiene autorización para realizar esta acción.", null);
    }

    /** Archivo que supera el limite del servidor: AN02 #10 (Cliente, 2MB) o AN02 #3 (resto de roles, 10MB). */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUpload(MaxUploadSizeExceededException ex) {
        return build(HttpStatus.BAD_REQUEST, esCliente() ? Mensajes.AN02_10 : Mensajes.AN02_03, null);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ErrorResponse> handleValidation(Exception ex) {
        BindingResult resultado = ex instanceof MethodArgumentNotValidException manv
                ? manv.getBindingResult()
                : ((BindException) ex).getBindingResult();
        List<FieldError> errores = resultado.getFieldErrors();

        List<String> detalles = errores.stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();

        boolean hayObligatorioFaltante = errores.stream()
                .anyMatch(fe -> "NotBlank".equals(fe.getCode()) || "NotNull".equals(fe.getCode()));
        // AN02 #7: el unico problema es el formato del correo.
        boolean soloCorreoInvalido = !hayObligatorioFaltante && !errores.isEmpty() && errores.stream()
                .allMatch(fe -> ("correoContacto".equals(fe.getField()) || "correoElectronico".equals(fe.getField()))
                        && "Email".equals(fe.getCode()));
        // AN02 #16: la nueva contrasena no cumple los requisitos de seguridad.
        boolean soloContrasenaInvalida = !hayObligatorioFaltante && !errores.isEmpty() && errores.stream()
                .allMatch(fe -> "nuevaContrasena".equals(fe.getField()) && "Pattern".equals(fe.getCode()));

        String mensaje = soloCorreoInvalido ? Mensajes.AN02_07
                : soloContrasenaInvalida ? Mensajes.AN02_16
                : Mensajes.AN02_01;
        return build(HttpStatus.BAD_REQUEST, mensaje, detalles);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado en el servidor.",
                List.of(ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage()));
    }

    private boolean esCliente() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_CLIENTE".equals(a.getAuthority()));
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String mensaje, List<String> detalles) {
        ErrorResponse body = ErrorResponse.builder()
                .codigo(status.value())
                .mensaje(mensaje)
                .fecha(LocalDateTime.now())
                .detalles(detalles)
                .build();
        return ResponseEntity.status(status).body(body);
    }
}
