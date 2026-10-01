package com.umg.quejasbancario.controller;

import com.umg.quejasbancario.dto.request.ForgotPasswordRequest;
import com.umg.quejasbancario.dto.request.LoginRequest;
import com.umg.quejasbancario.dto.request.ResetPasswordRequest;
import com.umg.quejasbancario.dto.response.LoginResponse;
import com.umg.quejasbancario.dto.response.MessageResponse;
import com.umg.quejasbancario.security.CustomUserDetails;
import com.umg.quejasbancario.service.AuthService;
import com.umg.quejasbancario.util.IpUtil;
import jakarta.servlet.http.HttpServletRequest;
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
    public LoginResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return authService.iniciarSesion(request, IpUtil.obtenerIp(httpRequest));
    }

    @PostMapping("/logout")
    public MessageResponse logout(@AuthenticationPrincipal CustomUserDetails principal, HttpServletRequest httpRequest) {
        authService.cerrarSesion(principal.getIdUsuario(), IpUtil.obtenerIp(httpRequest));
        return MessageResponse.of("Sesión cerrada correctamente.");
    }

    @PostMapping("/forgot-password")
    public MessageResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request, HttpServletRequest httpRequest) {
        authService.solicitarRecuperacion(request, IpUtil.obtenerIp(httpRequest));
        return MessageResponse.of("Si el correo se encuentra registrado, recibirá un enlace de recuperación en breve.");
    }

    @PostMapping("/reset-password")
    public MessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request, HttpServletRequest httpRequest) {
        authService.restablecerContrasena(request, IpUtil.obtenerIp(httpRequest));
        return MessageResponse.of("Su contraseña fue actualizada correctamente.");
    }
}
