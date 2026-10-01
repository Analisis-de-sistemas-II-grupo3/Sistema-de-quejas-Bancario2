package com.umg.quejasbancario.entity;

import com.umg.quejasbancario.entity.enums.EstadoSolicitudReasignacion;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * SOLICITUD_REASIGNACION (RN07, CU-05, CU-06).
 *
 * Extension respecto al DDL original: se agrega motivoRechazo (TEXT,
 * nullable) para registrar el motivo que el Supervisor ingresa al
 * rechazar la solicitud (CU-06, flujo alterno FA01). Ver extensions.sql.
 */
@Entity
@Table(name = "solicitud_reasignacion")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SolicitudReasignacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solicitud")
    private Integer idSolicitud;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_caso", nullable = false)
    private Caso caso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_agente_solicita", nullable = false)
    private Usuario agenteSolicita;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_supervisor_resuelve")
    private Usuario supervisorResuelve;

    @Column(name = "motivo", nullable = false, columnDefinition = "TEXT")
    private String motivo;

    @Column(name = "estado", nullable = false, length = 20)
    private EstadoSolicitudReasignacion estado;

    @Column(name = "fecha_solicitud", nullable = false, updatable = false)
    private LocalDateTime fechaSolicitud;

    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;

    /** Extension: motivo de rechazo ingresado por el Supervisor (CU-06 FA01). */
    @Column(name = "motivo_rechazo", columnDefinition = "TEXT")
    private String motivoRechazo;

    @PrePersist
    public void prePersist() {
        if (fechaSolicitud == null) fechaSolicitud = LocalDateTime.now();
        if (estado == null) estado = EstadoSolicitudReasignacion.PENDIENTE;
    }
}
