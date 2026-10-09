package com.umg.quejasbancario.controller;

import com.umg.quejasbancario.dto.request.ForgotPasswordRequest;
import com.umg.quejasbancario.dto.request.LoginRequest;
import com.umg.quejasbancario.dto.request.ResetPasswordRequest;
import com.umg.quejasbancario.dto.response.LoginResponse;
import com.umg.quejasbancario.dto.response.MessageResponse;
import com.umg.quejasbancario.security.CustomUserDetails;
import com.umg.quejasbancario.service.AuthService;
import com.umg.quejasbancario.util.IpUtil;
import com.umg.quejasbancario.util.Mensajes;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest,
                               HttpServletResponse respuesta) {
        LoginResponse login = authService.iniciarSesion(request, IpUtil.obtenerIp(httpRequest));
        Mensajes.enviar(respuesta, Mensajes.AN01_10_INICIO_SESION);   // AN01 #10
        return login;
    }

    @PostMapping("/logout")
    public MessageResponse logout(@AuthenticationPrincipal CustomUserDetails principal, HttpServletRequest httpRequest,
                                  HttpServletResponse respuesta) {
        authService.cerrarSesion(principal.getIdUsuario(), IpUtil.obtenerIp(httpRequest));
        Mensajes.enviar(respuesta, Mensajes.AN01_11_CIERRE_SESION);   // AN01 #11
        return MessageResponse.of(Mensajes.AN01_11_CIERRE_SESION);
    }

    @PostMapping("/forgot-password")
    public MessageResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request, HttpServletRequest httpRequest,
                                          HttpServletResponse respuesta) {
        authService.solicitarRecuperacion(request, IpUtil.obtenerIp(httpRequest));
        Mensajes.enviar(respuesta, Mensajes.AN01_12_ENLACE_ENVIADO);   // AN01 #12
        return MessageResponse.of(Mensajes.AN01_12_ENLACE_ENVIADO);
    }

    @PostMapping("/reset-password")
    public MessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request, HttpServletRequest httpRequest,
                                         HttpServletResponse respuesta) {
        authService.restablecerContrasena(request, IpUtil.obtenerIp(httpRequest));
        Mensajes.enviar(respuesta, Mensajes.AN01_13_CONTRASENA_ACTUALIZADA);   // AN01 #13
        return MessageResponse.of(Mensajes.AN01_13_CONTRASENA_ACTUALIZADA);
    }
}
