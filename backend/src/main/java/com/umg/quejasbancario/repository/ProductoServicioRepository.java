package com.umg.quejasbancario.repository;

import com.umg.quejasbancario.entity.ProductoServicio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoServicioRepository extends JpaRepository<ProductoServicio, Integer> {
    boolean existsByNombreIgnoreCase(String nombre);
}
