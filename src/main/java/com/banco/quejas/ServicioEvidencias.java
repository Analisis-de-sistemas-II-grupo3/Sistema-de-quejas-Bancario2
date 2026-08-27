package com.banco.quejas;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * CU03: verifica permisos y reglas, guarda los archivos y los asocia al folio.
 * El controlador llama a esta clase; la vista HTML nunca manipula archivos directamente.
 */
@Service
public class ServicioEvidencias {
    private static final long LIMITE_CLIENTE = 2L * 1024 * 1024;
    private static final long LIMITE_AGENTE = 10L * 1024 * 1024;
    private final RegistradorCasos casos;
    private final AlmacenAdjuntos almacen;
    private final Map<String, List<EvidenciaCaso>> evidenciasPorFolio = new ConcurrentHashMap<>();

    public ServicioEvidencias(RegistradorCasos casos, AlmacenAdjuntos almacen) {
        this.casos = casos;
        this.almacen = almacen;
    }

    public List<EvidenciaCaso> adjuntar(String folio, ContextoAutenticacion contexto, List<MultipartFile> archivos) {
        Caso caso = buscarCaso(folio);
        validarPermiso(caso, contexto);
        if (archivos == null || archivos.isEmpty() || archivos.stream().allMatch(a -> a == null || a.isEmpty())) {
            throw new ErrorEvidenciaException("Debe seleccionar al menos un archivo.");
        }
        long limite = limitePara(contexto.sesion().rol());
        for (MultipartFile archivo : archivos) almacen.validar(archivo, limite);

        List<EvidenciaCaso> nuevas = new ArrayList<>();
        try {
            for (MultipartFile archivo : archivos) {
                DocumentoAdjunto documento = almacen.guardar(archivo, limite);
                nuevas.add(new EvidenciaCaso(null, nombreOriginal(archivo), documento.ruta(), documento.tamanioBytes(),
                        documento.extension(), contexto.sesion().usuario(), LocalDateTime.now()));
            }
            evidenciasPorFolio.computeIfAbsent(caso.folio(), clave -> new ArrayList<>()).addAll(nuevas);
            casos.guardarEvidencias(caso.folio(), nuevas, contexto.sesion());
            casos.registrarCargaEvidencia(caso.folio(), contexto.sesion(), nuevas.size() + " archivo(s) adjuntado(s) al caso.");
            return List.copyOf(nuevas);
        } catch (RuntimeException error) {
            nuevas.forEach(e -> almacen.eliminar(new DocumentoAdjunto(e.rutaGuardada(), e.tamanioBytes(), e.extension())));
            throw error;
        }
    }

    public List<EvidenciaCaso> consultar(String folio, ContextoAutenticacion contexto) {
        Caso caso = buscarCaso(folio);
        validarPermiso(caso, contexto);
        List<EvidenciaCaso> evidencias = casos.consultarEvidencias(caso.folio());
        if (evidencias.isEmpty()) evidencias = evidenciasPorFolio.getOrDefault(caso.folio(), List.of());
        return evidencias.stream()
                .sorted(Comparator.comparing(EvidenciaCaso::fechaCarga).reversed()).toList();
    }

    public Caso buscarCaso(String folio) {
        return casos.buscarPorFolio(folio).orElseThrow(() -> new ErrorEvidenciaException("No se encontró el caso indicado."));
    }
    public ArchivoEvidencia descargar(String folio, long documentoId, ContextoAutenticacion contexto) {
        Caso caso = buscarCaso(folio);
        validarPermiso(caso, contexto);
        return casos.descargarEvidencia(caso.folio(), documentoId);
    }

    private void validarPermiso(Caso caso, ContextoAutenticacion contexto) {
        if (contexto == null || contexto.sesion() == null) throw new ErrorEvidenciaException("Debe iniciar sesión para adjuntar evidencia.");
        String rol = contexto.sesion().rol();
        if ("Agente de Atención".equalsIgnoreCase(rol)) return;
        boolean esClienteDelCaso = "Cliente".equalsIgnoreCase(rol) && contexto.cliente() != null
                && caso.clienteId().equals(contexto.cliente().id());
        if (!esClienteDelCaso) throw new ErrorEvidenciaException("No tiene permiso para adjuntar evidencia a este caso.");
    }

    private long limitePara(String rol) { return "Agente de Atención".equalsIgnoreCase(rol) ? LIMITE_AGENTE : LIMITE_CLIENTE; }
    private String nombreOriginal(MultipartFile archivo) { return archivo.getOriginalFilename() == null ? "archivo" : archivo.getOriginalFilename(); }
}
