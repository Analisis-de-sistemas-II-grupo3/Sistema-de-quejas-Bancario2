package com.umg.quejasbancario.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** Documentos de soporte adjuntos a un caso (RN10). */
@Entity
@Table(name = "documento_adjunto")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DocumentoAdjunto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_documento")
    private Integer idDocumento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_caso", nullable = false)
    private Caso caso;

    @Column(name = "nombre_archivo", nullable = false, length = 255)
    private String nombreArchivo;

    @Column(name = "ruta_archivo", nullable = false, length = 500)
    private String rutaArchivo;

    /** Tamano en bytes. */
    @Column(name = "tamano", nullable = false)
    private Integer tamano;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_carga", nullable = false)
    private Usuario usuarioCarga;

    @Column(name = "fecha_carga", nullable = false, updatable = false)
    private LocalDateTime fechaCarga;

    @PrePersist
    public void prePersist() {
        if (fechaCarga == null) fechaCarga = LocalDateTime.now();
    }
}
