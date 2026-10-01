package com.umg.quejasbancario.entity;

import jakarta.persistence.*;
import lombok.*;

/** Catalogo de tipos de caso: Queja, Reclamo, Denuncia, Sugerencia (RN03, RN05). */
@Entity
@Table(name = "tipo_caso")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TipoCaso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_caso")
    private Integer idTipoCaso;

    @Column(name = "nombre", nullable = false, unique = true, length = 30)
    private String nombre;

    /** Prefijo usado para el numero de caso (RN05): Q, R, D, S. */
    @Column(name = "prefijo", nullable = false, unique = true, length = 1)
    private String prefijo;
}
