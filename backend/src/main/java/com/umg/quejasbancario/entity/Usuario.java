package com.umg.quejasbancario.entity;

import com.umg.quejasbancario.entity.enums.EstadoUsuario;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * USUARIO: todos los actores humanos del sistema (RN01): Cliente/Denunciante,
 * Agente de Atencion, Administrador, Supervisor y Auditor.
 *
 * Nota de implementacion (extension respecto al DDL original del documento
 * "09_Base_de_Datos"): se agrega la columna sesion_activa, requerida por
 * RN06 para calcular la disponibilidad de un Agente ("su estado de sesion
 * se encuentra marcado como Activo"). Ver README / extensions.sql.
 */
@Entity
@Table(name = "usuario")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "nombre_usuario", nullable = false, unique = true, length = 50)
    private String nombreUsuario;

    @Column(name = "nombre_completo", nullable = false, length = 150)
    private String nombreCompleto;

    @Column(name = "correo_electronico", nullable = false, unique = true, length = 150)
    private String correoElectronico;

    @Column(name = "contrasena_hash", nullable = false, length = 255)
    private String contrasenaHash;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;

    @Column(name = "estado", nullable = false, length = 20)
    private EstadoUsuario estado;

    @Column(name = "intentos_fallidos", nullable = false)
    @Builder.Default
    private Integer intentosFallidos = 0;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    /** Extension: usado por RN06 para el criterio de disponibilidad de Agentes. */
    @Column(name = "sesion_activa", nullable = false)
    @Builder.Default
    private Boolean sesionActiva = false;

    /** Extension: fecha/hora hasta la cual la cuenta permanece bloqueada (RNF02). */
    @Column(name = "bloqueado_hasta")
    private LocalDateTime bloqueadoHasta;

    @PrePersist
    public void prePersist() {
        if (fechaCreacion == null) fechaCreacion = LocalDateTime.now();
        if (estado == null) estado = EstadoUsuario.ACTIVO;
        if (intentosFallidos == null) intentosFallidos = 0;
        if (sesionActiva == null) sesionActiva = false;
    }
}
