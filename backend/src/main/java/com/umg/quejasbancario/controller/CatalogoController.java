package com.umg.quejasbancario.controller;

import com.umg.quejasbancario.dto.request.CatalogoSimpleRequest;
import com.umg.quejasbancario.dto.request.TipoCasoRequest;
import com.umg.quejasbancario.dto.response.CatalogoItemResponse;
import com.umg.quejasbancario.security.CustomUserDetails;
import com.umg.quejasbancario.service.CatalogoService;
import com.umg.quejasbancario.util.IpUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CU-15 Gestionar Catalogos del Sistema. Lectura (GET) disponible para
 * cualquier usuario autenticado (los catalogos se usan en formularios de
 * Registrar Caso, etc.); escritura restringida a ADMINISTRADOR (ver
 * SecurityConfig).
 */
@RestController
@RequestMapping("/api/catalogos")
@RequiredArgsConstructor
public class CatalogoController {

    private final CatalogoService catalogoService;

    @GetMapping("/tipos-caso")
    public List<CatalogoItemResponse> listarTiposCaso() {
        return catalogoService.listarTiposCaso();
    }

    @PostMapping("/tipos-caso")
    public CatalogoItemResponse crearTipoCaso(@Valid @RequestBody TipoCasoRequest request,
                                               @AuthenticationPrincipal CustomUserDetails principal, HttpServletRequest httpRequest) {
        return catalogoService.crearTipoCaso(request, principal.getUsuario(), IpUtil.obtenerIp(httpRequest));
    }

    @PutMapping("/tipos-caso/{id}")
    public CatalogoItemResponse actualizarTipoCaso(@PathVariable Integer id, @Valid @RequestBody TipoCasoRequest request,
                                                     @AuthenticationPrincipal CustomUserDetails principal, HttpServletRequest httpRequest) {
        return catalogoService.actualizarTipoCaso(id, request, principal.getUsuario(), IpUtil.obtenerIp(httpRequest));
    }

    @GetMapping("/categorias")
    public List<CatalogoItemResponse> listarCategorias() {
        return catalogoService.listarCategorias();
    }

    @PostMapping("/categorias")
    public CatalogoItemResponse crearCategoria(@Valid @RequestBody CatalogoSimpleRequest request,
                                                @AuthenticationPrincipal CustomUserDetails principal, HttpServletRequest httpRequest) {
        return catalogoService.crearCategoria(request, principal.getUsuario(), IpUtil.obtenerIp(httpRequest));
    }

    @GetMapping("/productos")
    public List<CatalogoItemResponse> listarProductos() {
        return catalogoService.listarProductos();
    }

    @PostMapping("/productos")
    public CatalogoItemResponse crearProducto(@Valid @RequestBody CatalogoSimpleRequest request,
                                               @AuthenticationPrincipal CustomUserDetails principal, HttpServletRequest httpRequest) {
        return catalogoService.crearProducto(request, principal.getUsuario(), IpUtil.obtenerIp(httpRequest));
    }

    @GetMapping("/roles")
    public List<CatalogoItemResponse> listarRoles() {
        return catalogoService.listarRoles();
    }
}
