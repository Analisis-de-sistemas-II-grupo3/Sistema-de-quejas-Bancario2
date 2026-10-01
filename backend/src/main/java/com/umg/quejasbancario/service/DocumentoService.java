package com.umg.quejasbancario.service;

import com.umg.quejasbancario.config.AppProperties;
import com.umg.quejasbancario.entity.Caso;
import com.umg.quejasbancario.entity.DocumentoAdjunto;
import com.umg.quejasbancario.entity.Usuario;
import com.umg.quejasbancario.entity.enums.RolNombre;
import com.umg.quejasbancario.exception.AccesoDenegadoException;
import com.umg.quejasbancario.exception.BusinessRuleException;
import com.umg.quejasbancario.exception.ResourceNotFoundException;
import com.umg.quejasbancario.repository.DocumentoAdjuntoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Manejo de documentos de soporte adjuntos a un caso (RN10). El tamano
 * maximo permitido varia segun el actor que realiza la carga:
 *   - Cliente / Denunciante: 2 MB
 *   - Agente de Atencion / Administrador: 10 MB
 * Formatos permitidos: PDF o Imagen (jpg, jpeg, png).
 */
@Service
@RequiredArgsConstructor
public class DocumentoService {

    private static final long MB = 1024L * 1024L;
    private static final Set<String> EXTENSIONES_PERMITIDAS = Set.of("pdf", "jpg", "jpeg", "png");

    private final DocumentoAdjuntoRepository documentoAdjuntoRepository;
    private final AppProperties appProperties;

    public DocumentoAdjunto guardarDocumento(MultipartFile archivo, Caso caso, Usuario usuarioCarga) {
        validarFormato(archivo);
        validarTamano(archivo, usuarioCarga);

        try {
            Path dir = Path.of(appProperties.getUploads().getDir(), "casos", String.valueOf(caso.getIdCaso()));
            Files.createDirectories(dir);

            String extension = obtenerExtension(archivo.getOriginalFilename());
            String nombreGuardado = UUID.randomUUID() + "." + extension;
            Path destino = dir.resolve(nombreGuardado);
            Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

            DocumentoAdjunto documento = DocumentoAdjunto.builder()
                    .caso(caso)
                    .nombreArchivo(archivo.getOriginalFilename())
                    .rutaArchivo(destino.toString())
                    .tamano((int) archivo.getSize())
                    .usuarioCarga(usuarioCarga)
                    .build();

            return documentoAdjuntoRepository.save(documento);
        } catch (IOException e) {
            throw new BusinessRuleException("No fue posible guardar el documento adjunto. Intente nuevamente.");
        }
    }

    public List<DocumentoAdjunto> listarPorCaso(Integer idCaso) {
        return documentoAdjuntoRepository.findByCaso_IdCaso(idCaso);
    }

    /** Verifica autorizacion (RN14: un Cliente solo accede a documentos de sus propios casos) y devuelve el documento. */
    public DocumentoAdjunto obtenerParaDescarga(Integer idDocumento, Usuario solicitante) {
        DocumentoAdjunto documento = documentoAdjuntoRepository.findById(idDocumento)
                .orElseThrow(() -> new ResourceNotFoundException("El documento solicitado no existe."));

        String rol = solicitante.getRol().getNombreRol();
        if (RolNombre.CLIENTE.getValor().equalsIgnoreCase(rol)
                && !documento.getCaso().getCliente().getIdUsuario().equals(solicitante.getIdUsuario())) {
            throw new AccesoDenegadoException("No tiene autorización para descargar este documento.");
        }
        return documento;
    }

    private void validarFormato(MultipartFile archivo) {
        String extension = obtenerExtension(archivo.getOriginalFilename());
        if (extension == null || !EXTENSIONES_PERMITIDAS.contains(extension.toLowerCase())) {
            throw new BusinessRuleException("Por favor cargue un documento en formato PDF o imagen.");
        }
    }

    private void validarTamano(MultipartFile archivo, Usuario usuarioCarga) {
        String rolNombre = usuarioCarga.getRol().getNombreRol();
        long limiteBytes;
        String mensajeError;

        if (RolNombre.CLIENTE.getValor().equalsIgnoreCase(rolNombre)) {
            limiteBytes = 2 * MB;
            mensajeError = "Por favor verifique, el documento adjunto supera el tamaño máximo permitido de 2MB.";
        } else {
            // Agente de Atencion y Administrador: 10 MB
            limiteBytes = 10 * MB;
            mensajeError = "Verifique el tamaño del documento, el máximo permitido es 10MB.";
        }

        if (archivo.getSize() > limiteBytes) {
            throw new BusinessRuleException(mensajeError);
        }
    }

    private String obtenerExtension(String nombreArchivo) {
        if (nombreArchivo == null || !nombreArchivo.contains(".")) return null;
        return nombreArchivo.substring(nombreArchivo.lastIndexOf('.') + 1);
    }
}
