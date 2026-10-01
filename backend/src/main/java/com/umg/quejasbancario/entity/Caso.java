package com.umg.quejasbancario.entity;

import com.umg.quejasbancario.entity.enums.EstadoCaso;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * CASO: entidad central del sistema (quejas, reclamos, denuncias, sugerencias).
 *
 * Extensiones respecto al DDL original del documento "09_Base_de_Datos"
 * (ver database/extensions.sql, que debe ejecutarse contra la base ya
 * creada en Neon antes de levantar el backend):
 *   - nombreClienteCaso, identificacionCliente, correoContacto,
 *     telefonoContacto: datos de contacto capturados en CU-02 (RN04).
 *   - detalleResolucion: detalle de la solucion registrado por el Agente
 *     al resolver el caso (CU-08).
 *   - solicitudesReasignacionUsadas: contador de apoyo para RN07 (maximo
 *     2 solicitudes de reasignacion por caso).
 */
@Entity
@Table(name = "caso")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Caso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_caso")
    private Integer idCaso;

    /** Formato PREFIJO-CORRELATIVO(5 digitos)-ANIO, ej. Q-00001-2026 (RN05). */
    @Column(name = "numero_caso", nullable = false, unique = true, length = 20)
    private String numeroCaso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_caso", nullable = false)
    private TipoCaso tipoCaso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria")
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto")
    private ProductoServicio producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Usuario cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cuenta", nullable = false)
    private CuentaBancaria cuenta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_agente_asignado")
    private Usuario agenteAsignado;

    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "estado", nullable = false, length = 20)
    private EstadoCaso estado;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    // --- Datos de contacto del caso (RN04) ---
    @Column(name = "nombre_cliente_caso", length = 150)
    private String nombreClienteCaso;

    @Column(name = "identificacion_cliente", length = 30)
    private String identificacionCliente;

    @Column(name = "correo_contacto", length = 150)
    private String correoContacto;

    @Column(name = "telefono_contacto", length = 20)
    private String telefonoContacto;

    /** Extension: detalle de resolucion registrado por el Agente (CU-08). */
    @Column(name = "detalle_resolucion", columnDefinition = "TEXT")
    private String detalleResolucion;

    /** Contador de solicitudes de reasignacion ya utilizadas para este caso (RN07, max 2). */
    @Column(name = "solicitudes_reasignacion_usadas", nullable = false)
    @Builder.Default
    private Integer solicitudesReasignacionUsadas = 0;

    @PrePersist
    public void prePersist() {
        if (fechaRegistro == null) fechaRegistro = LocalDateTime.now();
        if (estado == null) estado = EstadoCaso.REGISTRADO;
        if (solicitudesReasignacionUsadas == null) solicitudesReasignacionUsadas = 0;
    }
}
