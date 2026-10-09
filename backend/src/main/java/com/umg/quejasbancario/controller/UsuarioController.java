package com.umg.quejasbancario.controller;

import com.umg.quejasbancario.dto.request.ActualizarUsuarioRequest;
import com.umg.quejasbancario.dto.request.CambiarEstadoUsuarioRequest;
import com.umg.quejasbancario.dto.request.CrearUsuarioRequest;
import com.umg.quejasbancario.dto.response.UsuarioResponse;
import com.umg.quejasbancario.security.CustomUserDetails;
import com.umg.quejasbancario.service.UsuarioService;
import com.umg.quejasbancario.util.IpUtil;
import com.umg.quejasbancario.util.Mensajes;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** CU-14 Gestionar Usuarios. Restringido a ADMINISTRADOR (ver SecurityConfig). */
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public List<UsuarioResponse> listar() {
        return usuarioService.listar();
    }

    @PostMapping
    public UsuarioResponse crear(@Valid @RequestBody CrearUsuarioRequest request,
                                  @AuthenticationPrincipal CustomUserDetails principal, HttpServletRequest httpRequest,
                                  HttpServletResponse respuesta) {
        UsuarioResponse usuario = usuarioService.crear(request, principal.getUsuario(), IpUtil.obtenerIp(httpRequest));
        Mensajes.enviar(respuesta, Mensajes.AN01_08_USUARIO_AGREGADO);   // AN01 #8
        return usuario;
    }

    @PutMapping("/{id}")
    public UsuarioResponse actualizar(@PathVariable Integer id, @RequestBody ActualizarUsuarioRequest request,
                                       @AuthenticationPrincipal CustomUserDetails principal, HttpServletRequest httpRequest,
                                       HttpServletResponse respuesta) {
        UsuarioResponse usuario = usuarioService.actualizar(id, request, principal.getUsuario(), IpUtil.obtenerIp(httpRequest));
        Mensajes.enviar(respuesta, Mensajes.AN01_16_USUARIO_ACTUALIZADO);   // AN01 #16
        return usuario;
    }

    @PatchMapping("/{id}/estado")
    public UsuarioResponse cambiarEstado(@PathVariable Integer id, @Valid @RequestBody CambiarEstadoUsuarioRequest request,
                                          @AuthenticationPrincipal CustomUserDetails principal, HttpServletRequest httpRequest,
                                          HttpServletResponse respuesta) {
        UsuarioResponse usuario = usuarioService.cambiarEstado(id, request, principal.getUsuario(), IpUtil.obtenerIp(httpRequest));
        Mensajes.enviar(respuesta, Mensajes.AN01_16_USUARIO_ACTUALIZADO);   // AN01 #16
        return usuario;
    }
}
