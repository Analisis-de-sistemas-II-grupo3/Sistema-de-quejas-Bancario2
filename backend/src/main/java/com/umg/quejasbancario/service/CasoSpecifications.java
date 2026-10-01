package com.umg.quejasbancario.service;

import com.umg.quejasbancario.entity.Caso;
import com.umg.quejasbancario.entity.enums.EstadoCaso;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

/** CU-10 Buscar y Filtrar Casos (RN14, RF30). */
public class CasoSpecifications {

    private CasoSpecifications() {}

    public static Specification<Caso> numeroCaso(String numeroCaso) {
        return (root, query, cb) -> numeroCaso == null || numeroCaso.isBlank() ? null :
                cb.like(cb.lower(root.get("numeroCaso")), "%" + numeroCaso.toLowerCase() + "%");
    }

    public static Specification<Caso> tipoCaso(Integer idTipoCaso) {
        return (root, query, cb) -> idTipoCaso == null ? null :
                cb.equal(root.get("tipoCaso").get("idTipoCaso"), idTipoCaso);
    }

    public static Specification<Caso> estado(EstadoCaso estado) {
        return (root, query, cb) -> estado == null ? null :
                cb.equal(root.get("estado"), estado);
    }

    public static Specification<Caso> cliente(String nombreCliente) {
        return (root, query, cb) -> nombreCliente == null || nombreCliente.isBlank() ? null :
                cb.like(cb.lower(root.get("nombreClienteCaso")), "%" + nombreCliente.toLowerCase() + "%");
    }

    public static Specification<Caso> agente(Integer idAgente) {
        return (root, query, cb) -> idAgente == null ? null :
                cb.equal(root.get("agenteAsignado").get("idUsuario"), idAgente);
    }

    public static Specification<Caso> fechaDesde(LocalDateTime desde) {
        return (root, query, cb) -> desde == null ? null :
                cb.greaterThanOrEqualTo(root.get("fechaRegistro"), desde);
    }

    public static Specification<Caso> fechaHasta(LocalDateTime hasta) {
        return (root, query, cb) -> hasta == null ? null :
                cb.lessThanOrEqualTo(root.get("fechaRegistro"), hasta);
    }
}
