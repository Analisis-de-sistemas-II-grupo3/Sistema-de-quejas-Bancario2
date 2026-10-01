package com.umg.quejasbancario.repository;

import com.umg.quejasbancario.entity.DocumentoAdjunto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DocumentoAdjuntoRepository extends JpaRepository<DocumentoAdjunto, Integer> {
    List<DocumentoAdjunto> findByCaso_IdCaso(Integer idCaso);
}
