package com.umg.quejasbancario.controller;

import com.umg.quejasbancario.dto.response.*;
import com.umg.quejasbancario.service.BitacoraConsultaService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/** CU-17 Consultar Bitacoras del Sistema (Auditor). Restringido a AUDITOR (ver SecurityConfig). */
@RestController
@RequestMapping("/api/bitacoras")
@RequiredArgsConstructor
public class BitacoraController {

    private final BitacoraConsultaService bitacoraConsultaService;

    @GetMapping("/casos")
    public List<BitacoraCasoResponse> bitacoraCasos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @RequestParam(required = false) Integer idUsuario,
            @RequestParam(required = false) String numeroCaso) {
        return bitacoraConsultaService.consultarBitacoraCasos(desde, hasta, idUsuario, numeroCaso);
    }

    @GetMapping("/accesos")
    public List<BitacoraAccesoResponse> bitacoraAccesos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @RequestParam(required = false) Integer idUsuario) {
        return bitacoraConsultaService.consultarBitacoraAccesos(desde, hasta, idUsuario);
    }

    @GetMapping("/usuarios")
    public List<BitacoraUsuarioResponse> bitacoraUsuarios(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @RequestParam(required = false) Integer idUsuario) {
        return bitacoraConsultaService.consultarBitacoraUsuarios(desde, hasta, idUsuario);
    }

    @GetMapping("/correos")
    public List<BitacoraCorreoResponse> bitacoraCorreos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @RequestParam(required = false) String numeroCaso) {
        return bitacoraConsultaService.consultarBitacoraCorreos(desde, hasta, numeroCaso);
    }
}
