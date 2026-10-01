package com.umg.quejasbancario.controller;

import com.umg.quejasbancario.dto.response.BitacoraCasoResponse;
import com.umg.quejasbancario.entity.Caso;
import com.umg.quejasbancario.service.BitacoraConsultaService;
import com.umg.quejasbancario.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * CU-12 Generar Reporte de Casos (Administrador/Supervisor) y CU-13 Generar
 * Reporte de Auditoria (Auditor). Formato de salida: excel (por defecto) o pdf.
 */
@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;
    private final BitacoraConsultaService bitacoraConsultaService;

    @GetMapping("/casos")
    public ResponseEntity<byte[]> reporteCasos(
            @RequestParam(defaultValue = "excel") String formato,
            @RequestParam(required = false) String numeroCaso,
            @RequestParam(required = false) Integer idTipoCaso,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {

        List<Caso> casos = reporteService.obtenerCasosParaReporte(numeroCaso, idTipoCaso, estado, desde, hasta);

        if ("pdf".equalsIgnoreCase(formato)) {
            return construirRespuesta(reporteService.reporteCasosPdf(casos), "reporte_casos.pdf", MediaType.APPLICATION_PDF);
        }
        return construirRespuesta(reporteService.reporteCasosExcel(casos), "reporte_casos.xlsx",
                MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }

    @GetMapping("/auditoria")
    public ResponseEntity<byte[]> reporteAuditoria(
            @RequestParam(defaultValue = "excel") String formato,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @RequestParam(required = false) Integer idUsuario,
            @RequestParam(required = false) String numeroCaso) {

        List<BitacoraCasoResponse> eventos = bitacoraConsultaService.consultarBitacoraCasos(desde, hasta, idUsuario, numeroCaso);

        if ("pdf".equalsIgnoreCase(formato)) {
            return construirRespuesta(reporteService.reporteAuditoriaPdf(eventos), "reporte_auditoria.pdf", MediaType.APPLICATION_PDF);
        }
        return construirRespuesta(reporteService.reporteAuditoriaExcel(eventos), "reporte_auditoria.xlsx",
                MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }

    private ResponseEntity<byte[]> construirRespuesta(byte[] contenido, String nombreArchivo, MediaType tipo) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().filename(nombreArchivo).build());
        return ResponseEntity.ok().headers(headers).contentType(tipo).body(contenido);
    }
}
