package com.umg.quejasbancario.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** Bitacora inmutable de cambios sobre usuarios (RN15). */
@Entity
@Table(name = "bitacora_usuario")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BitacoraUsuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_bitacora_usuario")
    private Long idBitacoraUsuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_afectado", nullable = false)
    private Usuario usuarioAfectado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_ejecuta", nullable = false)
    private Usuario usuarioEjecuta;

    @Column(name = "motivo", nullable = false, length = 255)
    private String motivo;

    @Column(name = "descripcion_evento", nullable = false, columnDefinition = "TEXT")
    private String descripcionEvento;

    @Column(name = "fecha_hora", nullable = false, updatable = false)
    private LocalDateTime fechaHora;

    @PrePersist
    public void prePersist() {
        if (fechaHora == null) fechaHora = LocalDateTime.now();
    }
}
