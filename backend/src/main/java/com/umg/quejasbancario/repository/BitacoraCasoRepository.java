package com.umg.quejasbancario.repository;

import com.umg.quejasbancario.entity.BitacoraCaso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;

public interface BitacoraCasoRepository extends JpaRepository<BitacoraCaso, Long>, JpaSpecificationExecutor<BitacoraCaso> {
    List<BitacoraCaso> findByCaso_IdCasoOrderByFechaHoraAsc(Integer idCaso);
}
