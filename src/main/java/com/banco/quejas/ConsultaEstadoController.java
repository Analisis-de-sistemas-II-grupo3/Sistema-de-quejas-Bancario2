/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.banco.quejas;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author José Chic
 */


/**
 * CU04 V1.3 - Consultar Estado de Caso.
 * Flujo básico: Cliente autenticado ve "Mis Casos" y su detalle.
 * FA01: consulta sin sesión, verificando folio + correo del caso.
 * FA02: mensaje AN02 #18 cuando el cliente no tiene casos registrados.
 */
@Controller
@RequestMapping("/consulta-estado")
public class ConsultaEstadoController {
    private final RegistradorCasos registrador;
    private final ProveedorSesionActual proveedorSesion;

    public ConsultaEstadoController(RegistradorCasos registrador, ProveedorSesionActual proveedorSesion) {
        this.registrador = registrador;
        this.proveedorSesion = proveedorSesion;
    }

    /** Paso 1-2 del flujo básico si hay sesión de Cliente; si no, ofrece FA01. */
    @GetMapping
    public String consultar(HttpServletRequest solicitudHttp, Model modelo) {
        ContextoAutenticacion contexto = proveedorSesion.obtener(solicitudHttp.getRemoteAddr());
        if (contexto != null && "Cliente".equalsIgnoreCase(contexto.sesion().rol())) {
            modelo.addAttribute("casos", registrador.consultarCasosCliente(contexto.cliente().id()));
            return "mis-casos";
        }
        modelo.addAttribute("formulario", new VerificacionConsultaFormulario());
        return "consulta-estado-publica";
    }

    /** FA01 pasos 2-4: valida folio + correo o DPI/NIT y continúa en el paso 5 (ver detalle). */
    @PostMapping("/publica")
    public String consultarPublico(@ModelAttribute VerificacionConsultaFormulario formulario, Model modelo) {
        if (esVacio(formulario.getFolio()) || esVacio(formulario.getDatoVerificacion())) {
            modelo.addAttribute("formulario", formulario);
            modelo.addAttribute("error", "Por favor ingrese los campos obligatorios.");
            return "consulta-estado-publica";
        }
        Optional<Caso> caso = registrador.buscarConVerificacion(formulario.getFolio(), formulario.getDatoVerificacion());
        if (caso.isEmpty()) {
            modelo.addAttribute("formulario", formulario);
            modelo.addAttribute("error", "El número de caso o el dato de verificación no son válidos.");
            return "consulta-estado-publica";
        }
        return prepararDetalle(caso.get(), true, modelo);
    }

    /** Paso 4-5 del flujo básico: "Ver Detalle" para un caso propio del cliente autenticado. */
    @GetMapping("/{folio}")
    public String verDetalle(@PathVariable String folio, HttpServletRequest solicitudHttp, Model modelo) {
        ContextoAutenticacion contexto = proveedorSesion.obtener(solicitudHttp.getRemoteAddr());
        if (contexto == null || !"Cliente".equalsIgnoreCase(contexto.sesion().rol())) return "redirect:/consulta-estado";
        Optional<Caso> caso = registrador.buscarPorFolio(folio);
        if (caso.isEmpty() || !caso.get().clienteId().equals(contexto.cliente().id())) {
            modelo.addAttribute("error", "No tiene autorización para consultar este caso.");
            modelo.addAttribute("casos", registrador.consultarCasosCliente(contexto.cliente().id()));
            return "mis-casos";
        }
        return prepararDetalle(caso.get(), false, modelo);
    }

    private String prepararDetalle(Caso caso, boolean origenPublico, Model modelo) {
        modelo.addAttribute("caso", caso);
        modelo.addAttribute("historial", registrador.consultarHistorial(caso.folio()));
        modelo.addAttribute("evidencias", registrador.consultarEvidencias(caso.folio()));
        modelo.addAttribute("origenPublico", origenPublico);
        return "caso-detalle";
    }

    private boolean esVacio(String valor) { return valor == null || valor.isBlank(); }
}
