package com.umg.quejasbancario.repository;

import com.umg.quejasbancario.entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RolRepository extends JpaRepository<Rol, Integer> {
    Optional<Rol> findByNombreRolIgnoreCase(String nombreRol);
}
