package com.banco.quejas;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

/** Pantallas web de CU03: ver las evidencias del caso y adjuntar una o varias nuevas. */
@Controller
@RequestMapping("/casos/{folio}/evidencias")
public class ControladorEvidencias {
    private final ServicioEvidencias servicio;
    private final ProveedorSesionActual proveedorSesion;

    public ControladorEvidencias(ServicioEvidencias servicio, ProveedorSesionActual proveedorSesion) {
        this.servicio = servicio;
        this.proveedorSesion = proveedorSesion;
    }

    @GetMapping
    public String mostrar(@PathVariable String folio, HttpServletRequest solicitud, Model modelo) {
        return prepararVista(folio, contexto(solicitud), modelo);
    }

    @PostMapping(consumes = "multipart/form-data")
    public String adjuntar(@PathVariable String folio, @RequestParam("archivos") List<MultipartFile> archivos,
                           HttpServletRequest solicitud, Model modelo) {
        try {
            List<EvidenciaCaso> nuevas = servicio.adjuntar(folio, contexto(solicitud), archivos);
            modelo.addAttribute("mensaje", nuevas.size() == 1 ? "La evidencia fue adjuntada correctamente."
                    : "Las " + nuevas.size() + " evidencias fueron adjuntadas correctamente.");
        } catch (ErrorEvidenciaException error) {
            modelo.addAttribute("error", error.getMessage());
        }
        return prepararVista(folio, contexto(solicitud), modelo);
    }

    /** Descarga un archivo únicamente después de comprobar el permiso sobre el caso. */
    @GetMapping("/{id}/descarga")
    public ResponseEntity<byte[]> descargar(@PathVariable String folio, @PathVariable long id,
                                             HttpServletRequest solicitud) {
        try {
            ArchivoEvidencia archivo = servicio.descargar(folio, id, contexto(solicitud));
            MediaType tipo = MediaType.parseMediaType(archivo.tipoContenido() == null
                    ? "application/octet-stream" : archivo.tipoContenido());
            return ResponseEntity.ok().contentType(tipo)
                    .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                            .filename(archivo.nombre(), StandardCharsets.UTF_8).build().toString())
                    .body(archivo.contenido());
        } catch (ErrorEvidenciaException error) {
            return ResponseEntity.notFound().build();
        }
    }

    private ContextoAutenticacion contexto(HttpServletRequest solicitud) {
        return proveedorSesion.obtener(solicitud.getRemoteAddr());
    }

    private String prepararVista(String folio, ContextoAutenticacion contexto, Model modelo) {
        try {
            Caso caso = servicio.buscarCaso(folio);
            modelo.addAttribute("caso", caso);
            modelo.addAttribute("evidencias", servicio.consultar(folio, contexto));
            modelo.addAttribute("limiteMb", "Agente de Atención".equalsIgnoreCase(contexto.sesion().rol()) ? 10 : 2);
            return "evidencias-caso";
        } catch (ErrorEvidenciaException error) {
            modelo.addAttribute("error", error.getMessage());
            return "evidencias-error";
        }
    }
}
