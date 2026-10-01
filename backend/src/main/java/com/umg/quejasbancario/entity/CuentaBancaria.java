package com.umg.quejasbancario.entity;

import com.umg.quejasbancario.entity.enums.EstadoCuenta;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cuenta_bancaria")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CuentaBancaria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cuenta")
    private Integer idCuenta;

    @Column(name = "numero_cuenta", nullable = false, unique = true, length = 30)
    private String numeroCuenta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "estado", nullable = false, length = 20)
    private EstadoCuenta estado;

    @PrePersist
    public void prePersist() {
        if (estado == null) estado = EstadoCuenta.ACTIVA;
    }
}
