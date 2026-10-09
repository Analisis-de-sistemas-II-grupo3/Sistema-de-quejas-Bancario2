package com.umg.quejasbancario.controller;

import com.umg.quejasbancario.dto.request.RegistrarCasoRequest;
import com.umg.quejasbancario.dto.request.ResolverCasoRequest;
import com.umg.quejasbancario.dto.response.CasoDetalleResponse;
import com.umg.quejasbancario.dto.response.CasoResumenResponse;
import com.umg.quejasbancario.dto.response.MessageResponse;
import com.umg.quejasbancario.security.CustomUserDetails;
import com.umg.quejasbancario.service.AtencionService;
import com.umg.quejasbancario.service.CasoService;
import com.umg.quejasbancario.util.IpUtil;
import com.umg.quejasbancario.util.Mensajes;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/casos")
@RequiredArgsConstructor
public class CasoController {

    private final CasoService casoService;
    private final AtencionService atencionService;

    // ---------------- Cliente (CU-02, CU-03) ----------------

    @PostMapping(value = "/registrar", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('CLIENTE')")
    public CasoDetalleResponse registrarCaso(@Valid @ModelAttribute RegistrarCasoRequest request,
                                              @RequestParam(value = "archivo", required = false) MultipartFile archivo,
                                              @AuthenticationPrincipal CustomUserDetails principal,
                                              HttpServletRequest httpRequest,
                                              HttpServletResponse respuesta) {
        CasoDetalleResponse caso = casoService.registrarCaso(request, archivo, principal.getUsuario(), IpUtil.obtenerIp(httpRequest));
        Mensajes.enviar(respuesta, Mensajes.casoRegistrado(caso.getNumeroCaso()));   // AN01 #1 (CU-02 paso 15)
        return caso;
    }

    @GetMapping("/mis-casos")
    @PreAuthorize("hasRole('CLIENTE')")
    public List<CasoResumenResponse> misCasos(@AuthenticationPrincipal CustomUserDetails principal) {
        return casoService.misCasos(principal.getUsuario());
    }

    @GetMapping("/mis-casos/{id}")
    @PreAuthorize("hasRole('CLIENTE')")
    public CasoDetalleResponse detalleMiCaso(@PathVariable Integer id, @AuthenticationPrincipal CustomUserDetails principal) {
        return casoService.detalleCasoCliente(id, principal.getUsuario());
    }

    // ---------------- Roles internos (CU-10) ----------------

    @GetMapping("/buscar")
    @PreAuthorize("hasAnyRole('AGENTE','ADMINISTRADOR','SUPERVISOR')")
    public List<CasoResumenResponse> buscarCasos(
            @RequestParam(required = false) String numeroCaso,
            @RequestParam(required = false) Integer idTipoCaso,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String nombreCliente,
            @RequestParam(required = false) Integer idAgente,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return casoService.buscarCasos(numeroCaso, idTipoCaso, estado, nombreCliente, idAgente, desde, hasta, principal.getUsuario());
    }

    /** Bandeja del Agente: sus propios casos (activos e historicos). */
    @GetMapping("/bandeja")
    @PreAuthorize("hasRole('AGENTE')")
    public List<CasoResumenResponse> bandejaAgente(@AuthenticationPrincipal CustomUserDetails principal) {
        return casoService.buscarCasos(null, null, null, null, principal.getIdUsuario(), null, null, principal.getUsuario());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('AGENTE','ADMINISTRADOR','SUPERVISOR','AUDITOR')")
    public CasoDetalleResponse detalleCaso(@PathVariable Integer id) {
        return casoService.detalleCasoInterno(id);
    }

    // ---------------- Atencion del Agente (CU-07, CU-08, CU-09) ----------------

    @PostMapping("/{id}/iniciar-atencion")
    @PreAuthorize("hasRole('AGENTE')")
    public MessageResponse iniciarAtencion(@PathVariable Integer id, @AuthenticationPrincipal CustomUserDetails principal,
                                           HttpServletRequest httpRequest, HttpServletResponse respuesta) {
        AtencionService.CambioEstado cambio = atencionService.iniciarAtencion(id, principal.getUsuario(), IpUtil.obtenerIp(httpRequest));
        return exitoCambioEstado(respuesta, cambio);
    }

    @PostMapping("/{id}/poner-en-espera")
    @PreAuthorize("hasRole('AGENTE')")
    public MessageResponse ponerEnEspera(@PathVariable Integer id, @RequestBody(required = false) Map<String, String> body,
                                          @AuthenticationPrincipal CustomUserDetails principal, HttpServletRequest httpRequest,
                                          HttpServletResponse respuesta) {
        String motivo = body != null ? body.get("motivo") : null;
        AtencionService.CambioEstado cambio = atencionService.ponerEnEspera(id, principal.getUsuario(), motivo, IpUtil.obtenerIp(httpRequest));
        return exitoCambioEstado(respuesta, cambio);
    }

    @PostMapping("/{id}/resolver")
    @PreAuthorize("hasRole('AGENTE')")
    public MessageResponse resolverCaso(@PathVariable Integer id, @Valid @RequestBody ResolverCasoRequest request,
                                         @AuthenticationPrincipal CustomUserDetails principal, HttpServletRequest httpRequest,
                                         HttpServletResponse respuesta) {
        AtencionService.CambioEstado cambio = atencionService.resolverCaso(id, principal.getUsuario(), request.getDetalleResolucion(), IpUtil.obtenerIp(httpRequest));
        String mensaje = Mensajes.resolucionRegistrada(cambio.numeroCaso());   // AN01 #4
        Mensajes.enviar(respuesta, mensaje);
        return MessageResponse.of(mensaje);
    }

    @PostMapping("/{id}/cerrar")
    @PreAuthorize("hasAnyRole('AGENTE','ADMINISTRADOR','SUPERVISOR')")
    public MessageResponse cerrarCaso(@PathVariable Integer id, @AuthenticationPrincipal CustomUserDetails principal,
                                      HttpServletRequest httpRequest, HttpServletResponse respuesta) {
        AtencionService.CambioEstado cambio = atencionService.cerrarCaso(id, principal.getUsuario(), IpUtil.obtenerIp(httpRequest));
        String mensaje = Mensajes.casoCerrado(cambio.numeroCaso());   // AN01 #5
        Mensajes.enviar(respuesta, mensaje);
        return MessageResponse.of(mensaje);
    }

    /** AN01 #3: "Se actualizo el estado del caso X de A a B" (CU-07). */
    private MessageResponse exitoCambioEstado(HttpServletResponse respuesta, AtencionService.CambioEstado cambio) {
        String mensaje = Mensajes.estadoActualizado(cambio.numeroCaso(), cambio.estadoAnterior(), cambio.estadoNuevo());
        Mensajes.enviar(respuesta, mensaje);
        return MessageResponse.of(mensaje);
    }
}
