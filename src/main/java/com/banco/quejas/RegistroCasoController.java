package com.banco.quejas;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

/** Controlador web exclusivo de CU02. CU01 reemplazará ProveedorSesionActual. */
@Controller
@RequestMapping("/casos")
public class RegistroCasoController {
    private final RegistradorCasos registrador;
    private final ProveedorSesionActual proveedorSesion;
    private final AlmacenAdjuntos almacenAdjuntos;

    public RegistroCasoController(RegistradorCasos registrador, ProveedorSesionActual proveedorSesion,
                                  AlmacenAdjuntos almacenAdjuntos) {
        this.registrador = registrador;
        this.proveedorSesion = proveedorSesion;
        this.almacenAdjuntos = almacenAdjuntos;
    }

    @GetMapping
    public String redirigirAFormulario() { return "redirect:/casos/nuevo"; }

    @GetMapping("/nuevo")
    public String mostrarFormulario(HttpServletRequest solicitudHttp, Model modelo) {
        ContextoAutenticacion contexto = proveedorSesion.obtener(solicitudHttp.getRemoteAddr());
        return prepararFormulario(modelo, new RegistroCasoFormulario(), contexto == null ? null : contexto.cliente(), null);
    }

    @PostMapping(consumes = "multipart/form-data")
    public String registrar(@ModelAttribute RegistroCasoFormulario formulario, @RequestParam(required = false) MultipartFile adjunto,
                            HttpServletRequest solicitudHttp, Model modelo) {
        DocumentoAdjunto documento = null;
        ContextoAutenticacion contexto = proveedorSesion.obtener(solicitudHttp.getRemoteAddr());
        try {
            documento = almacenAdjuntos.guardar(adjunto);
            Caso caso = registrador.registrar(contexto == null ? null : contexto.cliente(), contexto == null ? null : contexto.sesion(),
                    new SolicitudRegistroCaso(formulario.getTipo(), formulario.getProductoId(), formulario.getSucursalCanal(),
                            formulario.getFechaHecho(), formulario.getDescripcion(), documento));
            if (documento != null) {
                String nombre = adjunto.getOriginalFilename() == null ? "archivo" : adjunto.getOriginalFilename();
                registrador.guardarEvidencia(caso.folio(), new EvidenciaCaso(null, nombre, documento.ruta(),
                        documento.tamanioBytes(), documento.extension(), contexto.sesion().usuario(), java.time.LocalDateTime.now()), contexto.sesion());
            }
            modelo.addAttribute("caso", caso);
            return "caso-exito";
        } catch (ErrorEvidenciaException e) {
            // El navegador no permite volver a cargar automáticamente un archivo
            // (por seguridad), pero todos los demás campos sí se conservan.
            almacenAdjuntos.eliminar(documento);
            modelo.addAttribute("errorEvidencia", e.getMessage());
            modelo.addAttribute("mostrarPaso2", true);
            return prepararFormulario(modelo, formulario, contexto == null ? null : contexto.cliente(), null);
        } catch (RegistroCasoException e) {
            almacenAdjuntos.eliminar(documento);
            return prepararFormulario(modelo, formulario, contexto == null ? null : contexto.cliente(), e.getMessage());
        }
    }

    private String prepararFormulario(Model modelo, RegistroCasoFormulario formulario, Cliente cliente, String error) {
        modelo.addAttribute("formulario", formulario);
        modelo.addAttribute("tipos", TipoCaso.values());
        modelo.addAttribute("productos", registrador.consultarCatalogo());
        modelo.addAttribute("cliente", cliente);
        if (error != null) modelo.addAttribute("error", error);
        return "registro-caso";
    }
}
