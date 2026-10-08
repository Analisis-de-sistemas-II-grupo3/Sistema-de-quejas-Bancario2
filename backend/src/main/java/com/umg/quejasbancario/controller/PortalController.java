package com.umg.quejasbancario.controller;

import com.umg.quejasbancario.dto.response.CasoPublicoResponse;
import com.umg.quejasbancario.dto.response.InstitucionResponse;
import com.umg.quejasbancario.service.PortalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

/**
 * CU-00 Portal / Pagina de Inicio. Endpoints publicos (no requieren sesion);
 * habilitados en SecurityConfig bajo /api/publico/**.
 */
@RestController
@RequestMapping("/api/publico")
@RequiredArgsConstructor
public class PortalController {

    private final PortalService portalService;

    /** Identidad institucional del banco (RN17): nombre, logo, colores, mision, vision y valores. */
    @GetMapping("/institucion")
    public ResponseEntity<InstitucionResponse> institucion() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(5)).cachePublic())
                .body(portalService.obtenerInstitucion());
    }

    /** CU-00 FA01 / CU-03: estado de un caso consultado por su numero, sin iniciar sesion. */
    @GetMapping("/casos/{numeroCaso}/estado")
    public ResponseEntity<CasoPublicoResponse> estadoCaso(@PathVariable String numeroCaso) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(portalService.consultarEstadoCaso(numeroCaso));
    }
}
