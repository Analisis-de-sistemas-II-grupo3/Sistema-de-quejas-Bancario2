package com.umg.quejasbancario.controller;

import com.umg.quejasbancario.dto.request.RechazarReasignacionRequest;
import com.umg.quejasbancario.dto.request.SolicitarReasignacionRequest;
import com.umg.quejasbancario.dto.response.SolicitudReasignacionResponse;
import com.umg.quejasbancario.security.CustomUserDetails;
import com.umg.quejasbancario.service.ReasignacionService;
import com.umg.quejasbancario.util.IpUtil;
import com.umg.quejasbancario.util.Mensajes;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reasignaciones")
@RequiredArgsConstructor
public class ReasignacionController {

    private final ReasignacionService reasignacionService;

    @PostMapping("/casos/{idCaso}/solicitar")
    @PreAuthorize("hasRole('AGENTE')")
    public SolicitudReasignacionResponse solicitar(@PathVariable Integer idCaso,
                                                     @Valid @RequestBody SolicitarReasignacionRequest request,
                                                     @AuthenticationPrincipal CustomUserDetails principal,
                                                     HttpServletRequest httpRequest,
                                                     HttpServletResponse respuesta) {
        SolicitudReasignacionResponse solicitud =
                reasignacionService.solicitar(idCaso, principal.getUsuario(), request.getMotivo(), IpUtil.obtenerIp(httpRequest));
        Mensajes.enviar(respuesta, Mensajes.reasignacionSolicitada(solicitud.getNumeroCaso()));   // AN01 #6
        return solicitud;
    }

    @GetMapping("/pendientes")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public List<SolicitudReasignacionResponse> pendientes() {
        return reasignacionService.listarPendientes();
    }

    @PostMapping("/{idSolicitud}/aprobar")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public SolicitudReasignacionResponse aprobar(@PathVariable Integer idSolicitud,
                                                  @AuthenticationPrincipal CustomUserDetails principal,
                                                  HttpServletRequest httpRequest,
                                                  HttpServletResponse respuesta) {
        SolicitudReasignacionResponse solicitud =
                reasignacionService.aprobar(idSolicitud, principal.getUsuario(), IpUtil.obtenerIp(httpRequest));
        Mensajes.enviar(respuesta, Mensajes.reasignacionAprobada(solicitud.getNumeroCaso(), solicitud.getAgenteActual()));   // AN01 #7
        return solicitud;
    }

    @PostMapping("/{idSolicitud}/rechazar")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public SolicitudReasignacionResponse rechazar(@PathVariable Integer idSolicitud,
                                                   @RequestBody(required = false) RechazarReasignacionRequest request,
                                                   @AuthenticationPrincipal CustomUserDetails principal,
                                                   HttpServletRequest httpRequest,
                                                   HttpServletResponse respuesta) {
        String motivo = request != null ? request.getMotivoRechazo() : null;
        SolicitudReasignacionResponse solicitud =
                reasignacionService.rechazar(idSolicitud, principal.getUsuario(), motivo, IpUtil.obtenerIp(httpRequest));
        Mensajes.enviar(respuesta, Mensajes.reasignacionRechazada(solicitud.getNumeroCaso()));   // AN01 #14
        return solicitud;
    }
}
