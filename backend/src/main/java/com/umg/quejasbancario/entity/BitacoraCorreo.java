package com.umg.quejasbancario.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** Bitacora inmutable de notificaciones enviadas (RN15, CU-11). */
@Entity
@Table(name = "bitacora_correo")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BitacoraCorreo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_bitacora_correo")
    private Long idBitacoraCorreo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_caso")
    private Caso caso;

    @Column(name = "destinatario", nullable = false, length = 150)
    private String destinatario;

    @Column(name = "tipo_notificacion", nullable = false, length = 100)
    private String tipoNotificacion;

    @Column(name = "descripcion_evento", nullable = false, columnDefinition = "TEXT")
    private String descripcionEvento;

    @Column(name = "fecha_hora", nullable = false, updatable = false)
    private LocalDateTime fechaHora;

    @PrePersist
    public void prePersist() {
        if (fechaHora == null) fechaHora = LocalDateTime.now();
    }
}
