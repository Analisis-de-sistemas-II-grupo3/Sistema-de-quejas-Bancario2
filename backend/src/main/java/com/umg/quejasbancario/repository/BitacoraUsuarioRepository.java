package com.umg.quejasbancario.repository;

import com.umg.quejasbancario.entity.BitacoraUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BitacoraUsuarioRepository extends JpaRepository<BitacoraUsuario, Long>, JpaSpecificationExecutor<BitacoraUsuario> {
}
