package com.umg.quejasbancario.repository;

import com.umg.quejasbancario.entity.BitacoraAcceso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BitacoraAccesoRepository extends JpaRepository<BitacoraAcceso, Long>, JpaSpecificationExecutor<BitacoraAcceso> {
}
