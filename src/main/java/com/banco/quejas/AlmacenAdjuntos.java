package com.banco.quejas;

import java.io.IOException;
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
    private static final long MAXIMO_BYTES = 2L * 1024 * 1024;
    private static final Set<String> EXTENSIONES = Set.of("pdf", "jpg", "jpeg", "png");
    private final Path directorio;

    public AlmacenAdjuntos(@Value("${adjuntos.directorio:uploads}") String directorio) {
        this.directorio = Path.of(directorio).toAbsolutePath().normalize();
    }

    public DocumentoAdjunto guardar(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) return null;
        String nombreOriginal = archivo.getOriginalFilename() == null ? "archivo" : Path.of(archivo.getOriginalFilename()).getFileName().toString();
        int punto = nombreOriginal.lastIndexOf('.');
        String extension = punto < 0 ? "" : nombreOriginal.substring(punto + 1).toLowerCase(Locale.ROOT);
        if (!EXTENSIONES.contains(extension)) throw new RegistroCasoException("El documento debe ser PDF, JPG, JPEG o PNG.");
        if (archivo.getSize() > MAXIMO_BYTES) throw new RegistroCasoException("El documento adjunto no puede exceder 2 MB.");
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

    public void eliminar(DocumentoAdjunto adjunto) {
        if (adjunto == null) return;
        try { Files.deleteIfExists(adjunto.ruta()); }
        catch (IOException ignored) { }
    }
}
