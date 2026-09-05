package com.banco.quejas;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import javax.imageio.ImageIO;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/** Almacenamiento local temporal; posteriormente puede reemplazarse por S3/MinIO. */
@Service
public class AlmacenAdjuntos {
    private static final Set<String> EXTENSIONES = Set.of("pdf", "jpg", "jpeg", "png");
    private final Path directorio;

    public AlmacenAdjuntos(@Value("${adjuntos.directorio:uploads}") String directorio) {
        this.directorio = Path.of(directorio).toAbsolutePath().normalize();
    }

    public DocumentoAdjunto guardar(MultipartFile archivo) {
        return guardar(archivo, 2L * 1024 * 1024);
    }

    /** Guarda un archivo después de aplicar el límite que corresponde al rol del usuario. */
    public DocumentoAdjunto guardar(MultipartFile archivo, long limiteBytes) {
        if (archivo == null || archivo.isEmpty()) return null;
        validar(archivo, limiteBytes);
        String nombreOriginal = archivo.getOriginalFilename() == null ? "archivo" : Path.of(archivo.getOriginalFilename()).getFileName().toString();
        int punto = nombreOriginal.lastIndexOf('.');
        String extension = punto < 0 ? "" : nombreOriginal.substring(punto + 1).toLowerCase(Locale.ROOT);
        try {
            Files.createDirectories(directorio);
            Path destino = directorio.resolve(UUID.randomUUID() + "." + extension).normalize();
            if (!destino.startsWith(directorio)) throw new RegistroCasoException("Nombre de archivo no válido.");
            try (var contenido = archivo.getInputStream()) {
                Files.copy(contenido, destino, StandardCopyOption.REPLACE_EXISTING);
            }
            return new DocumentoAdjunto(destino, archivo.getSize(), extension);
        } catch (IOException e) {
            throw new RegistroCasoException("No fue posible guardar el documento adjunto.");
        }
    }

    /**
     * Valida formato, tamaño e integridad antes de escribir en el disco.
     * Un PDF debe tener su estructura básica, páginas y no estar cifrado.
     * Una imagen debe poder ser leída por ImageIO.
     */
    public void validar(MultipartFile archivo, long limiteBytes) {
        if (archivo == null || archivo.isEmpty()) throw new ErrorEvidenciaException("El archivo seleccionado está vacío.");
        String nombre = archivo.getOriginalFilename() == null ? "" : archivo.getOriginalFilename();
        int punto = nombre.lastIndexOf('.');
        String extension = punto < 0 ? "" : nombre.substring(punto + 1).toLowerCase(Locale.ROOT);
        if (!EXTENSIONES.contains(extension)) throw new ErrorEvidenciaException("Solo se permiten archivos PDF o imágenes JPG, JPEG y PNG.");
        if (archivo.getSize() > limiteBytes) throw new ErrorEvidenciaException("Cada archivo no puede exceder " + (limiteBytes / (1024 * 1024)) + " MB.");
        try {
            byte[] contenido = archivo.getBytes();
            if (contenido.length == 0) throw new ErrorEvidenciaException("El documento está vacío o no se puede leer.");
            if ("pdf".equals(extension)) validarPdf(contenido);
            else validarImagen(contenido);
        } catch (IOException error) {
            throw new ErrorEvidenciaException("No fue posible leer el documento adjunto.");
        }
    }

    private void validarPdf(byte[] contenido) {
        // Un PDF válido siempre comienza así. ISO-8859-1 conserva los bytes sin
        // modificar; no se usa la codificación por defecto de Windows.
        String texto = new String(contenido, StandardCharsets.ISO_8859_1);
        if (!texto.startsWith("%PDF-")) {
            throw new ErrorEvidenciaException("El archivo no tiene una estructura PDF válida.");
        }
        if (texto.lastIndexOf("%%EOF") < 0 || texto.lastIndexOf("startxref") < 0) {
            throw new ErrorEvidenciaException("El PDF está incompleto o dañado.");
        }
        if (texto.contains("/Encrypt")) {
            throw new ErrorEvidenciaException("El PDF está protegido con contraseña o restricciones de acceso.");
        }
        // /Type /Page identifica páginas individuales; /Pages es solamente el
        // contenedor y no debe aceptar por sí solo un documento vacío.
        if (!texto.matches("(?s).*?/Type\\s*/Page(?!s).*")) {
            throw new ErrorEvidenciaException("El PDF está vacío o no contiene páginas legibles.");
        }
    }

    private void validarImagen(byte[] contenido) {
        try {
            BufferedImage imagen = ImageIO.read(new ByteArrayInputStream(contenido));
            if (imagen == null || imagen.getWidth() < 1 || imagen.getHeight() < 1) {
                throw new ErrorEvidenciaException("La imagen está dañada o no se puede abrir.");
            }
        } catch (ErrorEvidenciaException error) {
            throw error;
        } catch (IOException error) {
            throw new ErrorEvidenciaException("La imagen está dañada o no se puede abrir.");
        }
    }

    public void eliminar(DocumentoAdjunto adjunto) {
        if (adjunto == null) return;
        try { Files.deleteIfExists(adjunto.ruta()); }
        catch (IOException ignored) { }
    }
}
