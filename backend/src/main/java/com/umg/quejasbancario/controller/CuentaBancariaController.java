package com.umg.quejasbancario.controller;

import com.umg.quejasbancario.dto.response.CuentaBancariaResponse;
import com.umg.quejasbancario.entity.enums.EstadoCuenta;
import com.umg.quejasbancario.repository.CuentaBancariaRepository;
import com.umg.quejasbancario.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Utilizado por el Cliente para seleccionar su cuenta activa al registrar un caso (RN16, CU-02). */
@RestController
@RequestMapping("/api/cuentas")
@RequiredArgsConstructor
public class CuentaBancariaController {

    private final CuentaBancariaRepository cuentaBancariaRepository;

    @GetMapping("/mis-cuentas")
    @PreAuthorize("hasRole('CLIENTE')")
    public List<CuentaBancariaResponse> misCuentas(@AuthenticationPrincipal CustomUserDetails principal) {
        return cuentaBancariaRepository.findByUsuario_IdUsuarioAndEstado(principal.getIdUsuario(), EstadoCuenta.ACTIVA).stream()
                .map(c -> CuentaBancariaResponse.builder()
                        .idCuenta(c.getIdCuenta())
                        .numeroCuenta(c.getNumeroCuenta())
                        .estado(c.getEstado().getValor())
                        .build())
                .toList();
    }
}
