package com.umg.quejasbancario.controller;

import com.umg.quejasbancario.dto.request.ParametroRequest;
import com.umg.quejasbancario.entity.ParametroSistema;
import com.umg.quejasbancario.security.CustomUserDetails;
import com.umg.quejasbancario.service.CatalogoService;
import com.umg.quejasbancario.util.IpUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** CU-16 Configurar Parametros del Sistema (Administrador). */
@RestController
@RequestMapping("/api/parametros")
@RequiredArgsConstructor
public class ParametroController {

    private final CatalogoService catalogoService;

    @GetMapping
    public List<ParametroSistema> listar() {
        return catalogoService.listarParametros();
    }

    @PutMapping("/{id}")
    public ParametroSistema actualizar(@PathVariable Integer id, @Valid @RequestBody ParametroRequest request,
                                        @AuthenticationPrincipal CustomUserDetails principal, HttpServletRequest httpRequest) {
        return catalogoService.actualizarParametro(id, request, principal.getUsuario(), IpUtil.obtenerIp(httpRequest));
    }
}
