package com.umg.quejasbancario.service;

import com.umg.quejasbancario.config.AppProperties;
import com.umg.quejasbancario.config.JwtProperties;
import com.umg.quejasbancario.dto.request.ForgotPasswordRequest;
import com.umg.quejasbancario.dto.request.LoginRequest;
import com.umg.quejasbancario.dto.request.ResetPasswordRequest;
import com.umg.quejasbancario.dto.response.LoginResponse;
import com.umg.quejasbancario.entity.PasswordResetToken;
import com.umg.quejasbancario.entity.Usuario;
import com.umg.quejasbancario.entity.enums.EstadoUsuario;
import com.umg.quejasbancario.entity.enums.TipoEventoAcceso;
import com.umg.quejasbancario.exception.BusinessRuleException;
import com.umg.quejasbancario.exception.CuentaBloqueadaException;
import com.umg.quejasbancario.repository.PasswordResetTokenRepository;
import com.umg.quejasbancario.repository.UsuarioRepository;
import com.umg.quejasbancario.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * CU-01 Gestion de Acceso y Sesion de Usuario: Iniciar Sesion, Cerrar Sesion
 * y Recuperar Contrasena.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;
    private final AppProperties appProperties;
    private final BitacoraRegistroService bitacoraRegistroService;
    private final NotificacionService notificacionService;

    /** 2.3.1 Iniciar Sesion. */
    // noRollbackFor: el contador de intentos fallidos (RF03 / AN02 #12) debe persistirse aunque se lance la excepcion.
    @Transactional(noRollbackFor = {BusinessRuleException.class, CuentaBloqueadaException.class})
    public LoginResponse iniciarSesion(LoginRequest request, String ip) {
        Usuario usuario = usuarioRepository.findByNombreUsuarioIgnoreCase(request.getNombreUsuario())
                .orElseThrow(() -> new BusinessRuleException("Usuario o contraseña incorrectos."));

        // FA02: bloqueo por intentos fallidos
        if (usuario.getBloqueadoHasta() != null && usuario.getBloqueadoHasta().isAfter(LocalDateTime.now())) {
            throw new CuentaBloqueadaException("Su cuenta ha sido bloqueada temporalmente por múltiples intentos fallidos de inicio de sesión.");
        }

        // FA03: usuario inactivo
        if (usuario.getEstado() != EstadoUsuario.ACTIVO) {
            throw new BusinessRuleException("Su usuario se encuentra inactivo. Contacte al Administrador.");
        }

        // Paso 3: validar credenciales - FA01
        if (!passwordEncoder.matches(request.getContrasena(), usuario.getContrasenaHash())) {
            registrarIntentoFallido(usuario);
            throw new BusinessRuleException("Usuario o contraseña incorrectos.");
        }

        // Login exitoso: reiniciar contador de intentos fallidos
        usuario.setIntentosFallidos(0);
        usuario.setBloqueadoHasta(null);
        usuario.setSesionActiva(true);
        usuarioRepository.save(usuario);

        String rolNombre = usuario.getRol().getNombreRol();
        String token = jwtUtil.generarToken(usuario.getIdUsuario(), usuario.getNombreUsuario(), rolNombre);

        bitacoraRegistroService.registrarEventoAcceso(usuario, ip, TipoEventoAcceso.INICIO_SESION,
                "El usuario inició sesión desde la IP registrada.");

        return LoginResponse.builder()
                .token(token)
                .tipo("Bearer")
                .idUsuario(usuario.getIdUsuario())
                .nombreUsuario(usuario.getNombreUsuario())
                .nombreCompleto(usuario.getNombreCompleto())
                .rol(rolNombre)
                .expiraEnMs(jwtProperties.getExpirationMs())
                .build();
    }

    private void registrarIntentoFallido(Usuario usuario) {
        int intentos = usuario.getIntentosFallidos() == null ? 0 : usuario.getIntentosFallidos();
        intentos++;
        usuario.setIntentosFallidos(intentos);

        int maxIntentos = appProperties.getSeguridad().getMaxIntentosFallidos();
        if (intentos >= maxIntentos) {
            usuario.setBloqueadoHasta(LocalDateTime.now().plusMinutes(appProperties.getSeguridad().getBloqueoMinutos()));
        }
        usuarioRepository.save(usuario);
    }

    /** 2.3.2 Cerrar Sesion. Al ser JWT sin estado, la "invalidacion" es a nivel de aplicacion (RNF05). */
    @Transactional
    public void cerrarSesion(Integer idUsuario, String ip) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new BusinessRuleException("Usuario no encontrado."));
        usuario.setSesionActiva(false);
        usuarioRepository.save(usuario);

        bitacoraRegistroService.registrarEventoAcceso(usuario, ip, TipoEventoAcceso.CIERRE_SESION,
                "El usuario cerró sesión desde la IP registrada.");
    }

    /** 2.3.3 Recuperar Contrasena - paso 1 a 7: solicitar enlace. */
    @Transactional
    public void solicitarRecuperacion(ForgotPasswordRequest request, String ip) {
        Usuario usuario = usuarioRepository.findByCorreoElectronicoIgnoreCase(request.getCorreoElectronico())
                .orElseThrow(() -> new BusinessRuleException("El correo ingresado no corresponde a ningún usuario registrado."));

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .usuario(usuario)
                .token(token)
                .fechaExpiracion(LocalDateTime.now().plusMinutes(appProperties.getSeguridad().getResetTokenMinutos()))
                .usado(false)
                .build();
        passwordResetTokenRepository.save(resetToken);

        String enlace = appProperties.getFrontend().getResetPasswordUrl() + "?token=" + token;
        notificacionService.enviarEnlaceRecuperacion(usuario.getCorreoElectronico(), enlace);

        bitacoraRegistroService.registrarEventoAcceso(usuario, ip, TipoEventoAcceso.INICIO_SESION,
                "Se generó un enlace de recuperación de contraseña para el usuario.");
    }

    /** 2.3.3 Recuperar Contrasena - paso 8 a 11: establecer nueva contrasena. */
    @Transactional
    public void restablecerContrasena(ResetPasswordRequest request, String ip) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new BusinessRuleException("El enlace de recuperación ha expirado. Por favor solicite uno nuevo."));

        if (Boolean.TRUE.equals(resetToken.getUsado()) || resetToken.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            throw new BusinessRuleException("El enlace de recuperación ha expirado. Por favor solicite uno nuevo.");
        }

        Usuario usuario = resetToken.getUsuario();
        usuario.setContrasenaHash(passwordEncoder.encode(request.getNuevaContrasena()));
        usuario.setIntentosFallidos(0);
        usuario.setBloqueadoHasta(null);
        usuarioRepository.save(usuario);

        resetToken.setUsado(true);
        passwordResetTokenRepository.save(resetToken);

        bitacoraRegistroService.registrarEventoAcceso(usuario, ip, TipoEventoAcceso.INICIO_SESION,
                "El usuario actualizó su contraseña mediante el enlace de recuperación.");
    }
}
