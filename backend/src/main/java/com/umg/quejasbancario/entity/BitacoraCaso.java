package com.umg.quejasbancario.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** Bitacora inmutable de transiciones de estado de un caso (RN15). */
@Entity
@Table(name = "bitacora_caso")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BitacoraCaso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_bitacora_caso")
    private Long idBitacoraCaso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_caso", nullable = false)
    private Caso caso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @Column(name = "rol_ejecuta", nullable = false, length = 30)
    private String rolEjecuta;

    @Column(name = "ip", nullable = false, length = 45)
    private String ip;

    @Column(name = "estado_anterior", length = 30)
    private String estadoAnterior;

    @Column(name = "estado_nuevo", nullable = false, length = 30)
    private String estadoNuevo;

    @Column(name = "descripcion_evento", nullable = false, columnDefinition = "TEXT")
    private String descripcionEvento;

    @Column(name = "fecha_hora", nullable = false, updatable = false)
    private LocalDateTime fechaHora;

    @PrePersist
    public void prePersist() {
        if (fechaHora == null) fechaHora = LocalDateTime.now();
    }
}
