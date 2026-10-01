package com.umg.quejasbancario.entity;

import com.umg.quejasbancario.entity.enums.TipoEventoAcceso;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** Bitacora inmutable de inicios/cierres de sesion (RN15). */
@Entity
@Table(name = "bitacora_acceso")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BitacoraAcceso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_bitacora_acceso")
    private Long idBitacoraAcceso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "ip", nullable = false, length = 45)
    private String ip;

    @Column(name = "tipo_evento", nullable = false, length = 30)
    private TipoEventoAcceso tipoEvento;

    @Column(name = "descripcion_evento", nullable = false, columnDefinition = "TEXT")
    private String descripcionEvento;

    @Column(name = "fecha_hora", nullable = false, updatable = false)
    private LocalDateTime fechaHora;

    @PrePersist
    public void prePersist() {
        if (fechaHora == null) fechaHora = LocalDateTime.now();
    }
}
