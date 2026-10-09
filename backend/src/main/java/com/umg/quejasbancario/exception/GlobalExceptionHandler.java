package com.umg.quejasbancario.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.util.List;

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
        return build(HttpStatus.UNAUTHORIZED, "Usuario o contrasena incorrectos.", null);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ErrorResponse> handleLocked(LockedException ex) {
        return build(HttpStatus.LOCKED, "Su cuenta ha sido bloqueada temporalmente por multiples intentos fallidos de inicio de sesion.", null);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> handleDisabled(DisabledException ex) {
        return build(HttpStatus.FORBIDDEN, "Su usuario se encuentra inactivo. Contacte al Administrador.", null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleSpringAccessDenied(AccessDeniedException ex) {
        return build(HttpStatus.FORBIDDEN, "No tiene autorizacion para realizar esta accion.", null);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUpload(MaxUploadSizeExceededException ex) {
        return build(HttpStatus.BAD_REQUEST, "Verifique el tamano del documento, supera el maximo permitido.", null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();
        // AN02 #7 si el unico problema es el formato del correo; en cualquier otro caso AN02 #1.
        boolean hayObligatorioFaltante = ex.getBindingResult().getFieldErrors().stream()
                .anyMatch(fe -> "NotBlank".equals(fe.getCode()) || "NotNull".equals(fe.getCode()));
        boolean soloCorreoInvalido = !hayObligatorioFaltante && ex.getBindingResult().getFieldErrors().stream()
                .allMatch(fe -> "correoContacto".equals(fe.getField()) && "Email".equals(fe.getCode()));
        String mensaje = soloCorreoInvalido
                ? "Por favor verifique, el correo electrónico ingresado no tiene un formato válido."
                : "Por favor ingrese los campos obligatorios.";
        return build(HttpStatus.BAD_REQUEST, mensaje, detalles);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error inesperado en el servidor.", List.of(ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage()));
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
