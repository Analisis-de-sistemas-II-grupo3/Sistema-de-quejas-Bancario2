package com.umg.quejasbancario.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "producto_servicio")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductoServicio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Integer idProducto;

    @Column(name = "nombre", nullable = false, unique = true, length = 100)
    private String nombre;
}
