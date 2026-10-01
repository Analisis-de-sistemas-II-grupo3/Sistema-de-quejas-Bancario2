package com.umg.quejasbancario.repository;

import com.umg.quejasbancario.entity.Caso;
import com.umg.quejasbancario.entity.enums.EstadoCaso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CasoRepository extends JpaRepository<Caso, Integer>, JpaSpecificationExecutor<Caso> {

    Optional<Caso> findByNumeroCaso(String numeroCaso);

    List<Caso> findByCliente_IdUsuario(Integer idCliente);

    List<Caso> findByAgenteAsignado_IdUsuario(Integer idAgente);

    /** Casos activos de un agente: cualquier estado distinto de Cerrado (RN06). */
    @Query("SELECT COUNT(c) FROM Caso c WHERE c.agenteAsignado.idUsuario = :idAgente AND c.estado <> com.umg.quejasbancario.entity.enums.EstadoCaso.CERRADO")
    long contarCasosActivosPorAgente(@Param("idAgente") Integer idAgente);

    /** Casos "Registrado" sin agente asignado (para el reintento periodico RF17). */
    List<Caso> findByEstadoAndAgenteAsignadoIsNull(EstadoCaso estado);

    /**
     * Correlativo mas alto usado por un tipo de caso en un anio dado, para
     * generar el siguiente numero de caso (RN05). Se calcula contando los
     * casos ya existentes con ese prefijo/anio.
     */
    @Query("SELECT COUNT(c) FROM Caso c WHERE c.tipoCaso.idTipoCaso = :idTipoCaso " +
           "AND c.fechaRegistro >= :inicioAnio AND c.fechaRegistro < :finAnio")
    long contarCasosPorTipoYAnio(@Param("idTipoCaso") Integer idTipoCaso,
                                  @Param("inicioAnio") LocalDateTime inicioAnio,
                                  @Param("finAnio") LocalDateTime finAnio);
}
