package com.umg.quejasbancario.repository;

import com.umg.quejasbancario.entity.TipoCaso;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TipoCasoRepository extends JpaRepository<TipoCaso, Integer> {
    Optional<TipoCaso> findByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCase(String nombre);
}
