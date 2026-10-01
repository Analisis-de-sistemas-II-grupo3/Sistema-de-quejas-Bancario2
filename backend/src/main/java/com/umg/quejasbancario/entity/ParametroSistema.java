package com.umg.quejasbancario.entity;

import jakarta.persistence.*;
import lombok.*;

/** Parametros configurables (RN11), p.ej. limite_casos_activos_agente (RN06, RN16). */
@Entity
@Table(name = "parametro_sistema")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ParametroSistema {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_parametro")
    private Integer idParametro;

    @Column(name = "nombre_parametro", nullable = false, unique = true, length = 100)
    private String nombreParametro;

    @Column(name = "valor", nullable = false, length = 50)
    private String valor;
}
