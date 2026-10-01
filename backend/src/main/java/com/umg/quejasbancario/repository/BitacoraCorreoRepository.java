package com.umg.quejasbancario.repository;

import com.umg.quejasbancario.entity.BitacoraCorreo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BitacoraCorreoRepository extends JpaRepository<BitacoraCorreo, Long>, JpaSpecificationExecutor<BitacoraCorreo> {
}
