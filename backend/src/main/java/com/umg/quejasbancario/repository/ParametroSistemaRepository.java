package com.umg.quejasbancario.repository;

import com.umg.quejasbancario.entity.ParametroSistema;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ParametroSistemaRepository extends JpaRepository<ParametroSistema, Integer> {
    Optional<ParametroSistema> findByNombreParametroIgnoreCase(String nombre);
}
