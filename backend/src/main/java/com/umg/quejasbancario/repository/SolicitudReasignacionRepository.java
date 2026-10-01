package com.umg.quejasbancario.repository;

import com.umg.quejasbancario.entity.SolicitudReasignacion;
import com.umg.quejasbancario.entity.enums.EstadoSolicitudReasignacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SolicitudReasignacionRepository extends JpaRepository<SolicitudReasignacion, Integer> {
    List<SolicitudReasignacion> findByEstado(EstadoSolicitudReasignacion estado);
    Optional<SolicitudReasignacion> findByCaso_IdCasoAndEstado(Integer idCaso, EstadoSolicitudReasignacion estado);
    long countByCaso_IdCaso(Integer idCaso);
}
