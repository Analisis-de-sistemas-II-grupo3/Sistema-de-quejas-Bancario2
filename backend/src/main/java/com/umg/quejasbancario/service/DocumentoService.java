package com.umg.quejasbancario.service;

import com.itextpdf.kernel.exceptions.BadPasswordException;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Manejo de documentos de soporte adjuntos a un caso (RN10). El tamano
 * maximo permitido varia segun el actor que realiza la carga:
 *   - Cliente / Denunciante: 2 MB
 *   - Agente de Atencion / Administrador: 10 MB
 * Formatos permitidos: PDF o Imagen (jpg, jpeg, png).
 *
 * Ademas de formato y tamano, se valida el CONTENIDO del archivo (no solo
 * la extension): un PDF vacio (0 paginas), protegido con contrasena, o
 * corrupto es rechazado; una imagen corrupta o vacia tambien.
 */
@Service
@RequiredArgsConstructor
public class DocumentoService {

    /** AN02 #2: unico mensaje documentado para un adjunto que no es un PDF o imagen valido. */
    private static final String MSG_FORMATO_INVALIDO = "Por favor cargue un documento en formato PDF o imagen.";

    private static final long MB = 1024L * 1024L;
    private static final Set<String> EXTENSIONES_PERMITIDAS = Set.of("pdf", "jpg", "jpeg", "png");

    private final DocumentoAdjuntoRepository documentoAdjuntoRepository;
    private final AppProperties appProperties;

    public DocumentoAdjunto guardarDocumento(MultipartFile archivo, Caso caso, Usuario usuarioCarga) {
        String extension = obtenerExtension(archivo.getOriginalFilename());
        validarFormato(extension);
        validarTamano(archivo, usuarioCarga);

        byte[] contenido;
        try {
            contenido = archivo.getBytes();
        } catch (IOException e) {
            throw new BusinessRuleException("No fue posible leer el documento adjunto. Intente nuevamente.");
        }

        validarContenido(contenido, extension);

        try {
            Path dir = Path.of(appProperties.getUploads().getDir(), "casos", String.valueOf(caso.getIdCaso()));
            Files.createDirectories(dir);

            String nombreGuardado = UUID.randomUUID() + "." + extension;
            Path destino = dir.resolve(nombreGuardado);
            Files.write(destino, contenido);

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

    private void validarFormato(String extension) {
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

    /** Valida que el archivo tenga contenido real y legible, no solo la extension correcta. */
    private void validarContenido(byte[] contenido, String extension) {
        if (contenido == null || contenido.length == 0) {
            throw new BusinessRuleException(MSG_FORMATO_INVALIDO);
        }

        validarFirma(contenido, extension);

        if ("pdf".equalsIgnoreCase(extension)) {
            validarPdf(contenido);
        } else {
            validarImagen(contenido);
        }
    }

    /**
     * Verifica que los primeros bytes del archivo (su "firma") correspondan a la
     * extension declarada. Evita que un archivo de otro tipo (ej. un .txt o un .exe
     * renombrado a .pdf, o un PNG renombrado a .jpg) pase solo por su nombre.
     */
    private void validarFirma(byte[] contenido, String extension) {
        String ext = extension.toLowerCase();
        boolean coincide = switch (ext) {
            case "pdf" -> contienePdfEnCabecera(contenido);
            case "png" -> empiezaCon(contenido, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A);
            case "jpg", "jpeg" -> empiezaCon(contenido, 0xFF, 0xD8, 0xFF);
            default -> false;
        };
        if (!coincide) {
            throw new BusinessRuleException(
                    MSG_FORMATO_INVALIDO);
        }
    }

    /** Un PDF valido lleva "%PDF-" al inicio (los lectores toleran algo de texto previo, hasta 1024 bytes). */
    private boolean contienePdfEnCabecera(byte[] contenido) {
        int limite = Math.min(contenido.length, 1024);
        String cabecera = new String(contenido, 0, limite, java.nio.charset.StandardCharsets.ISO_8859_1);
        return cabecera.contains("%PDF-");
    }

    private boolean empiezaCon(byte[] contenido, int... firma) {
        if (contenido.length < firma.length) return false;
        for (int i = 0; i < firma.length; i++) {
            if ((contenido[i] & 0xFF) != firma[i]) return false;
        }
        return true;
    }

    private void validarPdf(byte[] contenido) {
        try (PdfReader reader = new PdfReader(new ByteArrayInputStream(contenido))) {
            PdfDocument pdfDoc = new PdfDocument(reader);
            int paginas = pdfDoc.getNumberOfPages();
            pdfDoc.close();
            if (paginas == 0) {
                throw new BusinessRuleException(MSG_FORMATO_INVALIDO);
            }
        } catch (BadPasswordException e) {
            throw new BusinessRuleException(MSG_FORMATO_INVALIDO);
        } catch (BusinessRuleException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessRuleException(MSG_FORMATO_INVALIDO);
        }
    }

    private void validarImagen(byte[] contenido) {
        try {
            BufferedImage imagen = ImageIO.read(new ByteArrayInputStream(contenido));
            if (imagen == null || imagen.getWidth() == 0 || imagen.getHeight() == 0) {
                throw new BusinessRuleException(MSG_FORMATO_INVALIDO);
            }
        } catch (BusinessRuleException e) {
            throw e;
        } catch (IOException e) {
            throw new BusinessRuleException(MSG_FORMATO_INVALIDO);
        }
    }

    private String obtenerExtension(String nombreArchivo) {
        if (nombreArchivo == null || !nombreArchivo.contains(".")) return null;
        return nombreArchivo.substring(nombreArchivo.lastIndexOf('.') + 1);
    }
}
