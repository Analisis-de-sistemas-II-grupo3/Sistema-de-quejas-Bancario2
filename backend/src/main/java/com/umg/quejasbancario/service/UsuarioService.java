package com.umg.quejasbancario.service;

import com.umg.quejasbancario.dto.request.ActualizarUsuarioRequest;
import com.umg.quejasbancario.dto.request.CambiarEstadoUsuarioRequest;
import com.umg.quejasbancario.dto.request.CrearUsuarioRequest;
import com.umg.quejasbancario.dto.response.UsuarioResponse;
import com.umg.quejasbancario.entity.Rol;
import com.umg.quejasbancario.entity.Usuario;
import com.umg.quejasbancario.entity.enums.EstadoUsuario;
import com.umg.quejasbancario.entity.enums.RolNombre;
import com.umg.quejasbancario.exception.BusinessRuleException;
import com.umg.quejasbancario.exception.ResourceNotFoundException;
import com.umg.quejasbancario.repository.CasoRepository;
import com.umg.quejasbancario.repository.RolRepository;
import com.umg.quejasbancario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;

/** CU-14 Gestionar Usuarios (Administrador). */
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private static final String CARACTERES_TEMPORAL = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
    private final SecureRandom random = new SecureRandom();

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final CasoRepository casoRepository;
    private final PasswordEncoder passwordEncoder;
    private final BitacoraRegistroService bitacoraRegistroService;
    private final NotificacionService notificacionService;

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll().stream().map(this::mapear).toList();
    }

    @Transactional
    public UsuarioResponse crear(CrearUsuarioRequest request, Usuario administrador, String ip) {
        if (usuarioRepository.existsByNombreUsuarioIgnoreCase(request.getNombreUsuario())) {
            throw new BusinessRuleException("El nombre de usuario ingresado ya existe en el sistema.");
        }
        if (usuarioRepository.existsByCorreoElectronicoIgnoreCase(request.getCorreoElectronico())) {
            throw new BusinessRuleException("El correo electrónico ingresado ya está registrado.");
        }

        Rol rol = rolRepository.findByNombreRolIgnoreCase(request.getRol())
                .orElseThrow(() -> new BusinessRuleException("El rol seleccionado no es válido."));

        String contrasenaTemporal = request.getContrasenaInicial() != null && !request.getContrasenaInicial().isBlank()
                ? request.getContrasenaInicial()
                : generarContrasenaTemporal();

        Usuario usuario = Usuario.builder()
                .nombreUsuario(request.getNombreUsuario())
                .nombreCompleto(request.getNombreCompleto())
                .correoElectronico(request.getCorreoElectronico())
                .contrasenaHash(passwordEncoder.encode(contrasenaTemporal))
                .rol(rol)
                .estado(EstadoUsuario.ACTIVO)
                .build();
        usuario = usuarioRepository.save(usuario);

        bitacoraRegistroService.registrarEventoUsuario(usuario, administrador, "Creación de usuario",
                "El Administrador '" + administrador.getNombreCompleto() + "' creó el usuario '" + usuario.getNombreUsuario() + "' con rol '" + rol.getNombreRol() + "'.");

        notificacionService.enviarEnlaceRecuperacion(usuario.getCorreoElectronico(),
                "Su usuario en el Sistema de Quejas Bancario es '" + usuario.getNombreUsuario()
                        + "' y su contraseña temporal es '" + contrasenaTemporal + "'. Se recomienda cambiarla al ingresar.");

        return mapear(usuario);
    }

    @Transactional
    public UsuarioResponse actualizar(Integer idUsuario, ActualizarUsuarioRequest request, Usuario administrador, String ip) {
        Usuario usuario = obtener(idUsuario);

        if (request.getNombreCompleto() != null && !request.getNombreCompleto().isBlank()) {
            usuario.setNombreCompleto(request.getNombreCompleto());
        }
        if (request.getCorreoElectronico() != null && !request.getCorreoElectronico().isBlank()) {
            usuario.setCorreoElectronico(request.getCorreoElectronico());
        }
        if (request.getRol() != null && !request.getRol().isBlank()) {
            Rol rol = rolRepository.findByNombreRolIgnoreCase(request.getRol())
                    .orElseThrow(() -> new BusinessRuleException("El rol seleccionado no es válido."));
            usuario.setRol(rol);
        }
        usuario = usuarioRepository.save(usuario);

        bitacoraRegistroService.registrarEventoUsuario(usuario, administrador, "Actualización de datos",
                "El Administrador '" + administrador.getNombreCompleto() + "' actualizó los datos del usuario '" + usuario.getNombreUsuario() + "'.");

        return mapear(usuario);
    }

    /** RN13: un Agente con casos activos sin reasignar no puede inactivarse (FA04). */
    @Transactional
    public UsuarioResponse cambiarEstado(Integer idUsuario, CambiarEstadoUsuarioRequest request, Usuario administrador, String ip) {
        Usuario usuario = obtener(idUsuario);
        EstadoUsuario nuevoEstado = EstadoUsuario.fromValor(request.getEstado());

        if (nuevoEstado == EstadoUsuario.INACTIVO
                && RolNombre.AGENTE.getValor().equalsIgnoreCase(usuario.getRol().getNombreRol())) {
            long casosActivos = casoRepository.contarCasosActivosPorAgente(usuario.getIdUsuario());
            if (casosActivos > 0) {
                throw new BusinessRuleException(
                        "No es posible inactivar al Agente porque tiene casos activos asignados. Reasigne sus casos antes de continuar.");
            }
        }

        usuario.setEstado(nuevoEstado);
        if (nuevoEstado == EstadoUsuario.INACTIVO) {
            usuario.setSesionActiva(false);
        }
        usuario = usuarioRepository.save(usuario);

        bitacoraRegistroService.registrarEventoUsuario(usuario, administrador, "Cambio de estado",
                "El Administrador '" + administrador.getNombreCompleto() + "' cambió el estado del usuario '"
                        + usuario.getNombreUsuario() + "' a '" + nuevoEstado.getValor() + "'.");

        return mapear(usuario);
    }

    private Usuario obtener(Integer idUsuario) {
        return usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("El usuario indicado no existe."));
    }

    private String generarContrasenaTemporal() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append(CARACTERES_TEMPORAL.charAt(random.nextInt(CARACTERES_TEMPORAL.length())));
        }
        return sb.toString();
    }

    private UsuarioResponse mapear(Usuario u) {
        return UsuarioResponse.builder()
                .idUsuario(u.getIdUsuario())
                .nombreUsuario(u.getNombreUsuario())
                .nombreCompleto(u.getNombreCompleto())
                .correoElectronico(u.getCorreoElectronico())
                .rol(u.getRol().getNombreRol())
                .estado(u.getEstado().getValor())
                .fechaCreacion(u.getFechaCreacion())
                .build();
    }
}
