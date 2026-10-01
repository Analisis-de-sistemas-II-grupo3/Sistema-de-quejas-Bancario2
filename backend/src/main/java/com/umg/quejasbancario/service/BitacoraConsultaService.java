package com.umg.quejasbancario.service;

import com.umg.quejasbancario.dto.response.*;
import com.umg.quejasbancario.entity.BitacoraAcceso;
import com.umg.quejasbancario.entity.BitacoraCaso;
import com.umg.quejasbancario.entity.BitacoraCorreo;
import com.umg.quejasbancario.entity.BitacoraUsuario;
import com.umg.quejasbancario.repository.BitacoraAccesoRepository;
import com.umg.quejasbancario.repository.BitacoraCasoRepository;
import com.umg.quejasbancario.repository.BitacoraCorreoRepository;
import com.umg.quejasbancario.repository.BitacoraUsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** CU-17 Consultar Bitacoras del Sistema (Auditor). RN15 / RN14 (filtros por fecha y usuario). */
@Service
@RequiredArgsConstructor
public class BitacoraConsultaService {

    private final BitacoraCasoRepository bitacoraCasoRepository;
    private final BitacoraAccesoRepository bitacoraAccesoRepository;
    private final BitacoraUsuarioRepository bitacoraUsuarioRepository;
    private final BitacoraCorreoRepository bitacoraCorreoRepository;

    @Transactional(readOnly = true)
    public List<BitacoraCasoResponse> consultarBitacoraCasos(LocalDateTime desde, LocalDateTime hasta, Integer idUsuario, String numeroCaso) {
        Specification<BitacoraCaso> spec = Specification.where(fechaEntre("fechaHora", desde, hasta));
        if (idUsuario != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("usuario").get("idUsuario"), idUsuario));
        }
        if (numeroCaso != null && !numeroCaso.isBlank()) {
            spec = spec.and((root, q, cb) -> cb.like(cb.lower(root.get("caso").get("numeroCaso")), "%" + numeroCaso.toLowerCase() + "%"));
        }
        return bitacoraCasoRepository.findAll(spec).stream()
                .map(b -> BitacoraCasoResponse.builder()
                        .id(b.getIdBitacoraCaso())
                        .numeroCaso(b.getCaso().getNumeroCaso())
                        .rolEjecuta(b.getRolEjecuta())
                        .usuario(b.getUsuario() != null ? b.getUsuario().getNombreCompleto() : "Sistema")
                        .ip(b.getIp())
                        .estadoAnterior(b.getEstadoAnterior())
                        .estadoNuevo(b.getEstadoNuevo())
                        .descripcionEvento(b.getDescripcionEvento())
                        .fechaHora(b.getFechaHora())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BitacoraAccesoResponse> consultarBitacoraAccesos(LocalDateTime desde, LocalDateTime hasta, Integer idUsuario) {
        Specification<BitacoraAcceso> spec = Specification.where(fechaEntre("fechaHora", desde, hasta));
        if (idUsuario != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("usuario").get("idUsuario"), idUsuario));
        }
        return bitacoraAccesoRepository.findAll(spec).stream()
                .map(b -> BitacoraAccesoResponse.builder()
                        .id(b.getIdBitacoraAcceso())
                        .usuario(b.getUsuario().getNombreCompleto())
                        .ip(b.getIp())
                        .tipoEvento(b.getTipoEvento().getValor())
                        .descripcionEvento(b.getDescripcionEvento())
                        .fechaHora(b.getFechaHora())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BitacoraUsuarioResponse> consultarBitacoraUsuarios(LocalDateTime desde, LocalDateTime hasta, Integer idUsuario) {
        Specification<BitacoraUsuario> spec = Specification.where(fechaEntre("fechaHora", desde, hasta));
        if (idUsuario != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("usuarioAfectado").get("idUsuario"), idUsuario));
        }
        return bitacoraUsuarioRepository.findAll(spec).stream()
                .map(b -> BitacoraUsuarioResponse.builder()
                        .id(b.getIdBitacoraUsuario())
                        .usuarioAfectado(b.getUsuarioAfectado().getNombreCompleto())
                        .usuarioEjecuta(b.getUsuarioEjecuta().getNombreCompleto())
                        .motivo(b.getMotivo())
                        .descripcionEvento(b.getDescripcionEvento())
                        .fechaHora(b.getFechaHora())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BitacoraCorreoResponse> consultarBitacoraCorreos(LocalDateTime desde, LocalDateTime hasta, String numeroCaso) {
        Specification<BitacoraCorreo> spec = Specification.where(fechaEntre("fechaHora", desde, hasta));
        if (numeroCaso != null && !numeroCaso.isBlank()) {
            spec = spec.and((root, q, cb) -> cb.like(cb.lower(root.get("caso").get("numeroCaso")), "%" + numeroCaso.toLowerCase() + "%"));
        }
        return bitacoraCorreoRepository.findAll(spec).stream()
                .map(b -> BitacoraCorreoResponse.builder()
                        .id(b.getIdBitacoraCorreo())
                        .numeroCaso(b.getCaso() != null ? b.getCaso().getNumeroCaso() : null)
                        .destinatario(b.getDestinatario())
                        .tipoNotificacion(b.getTipoNotificacion())
                        .descripcionEvento(b.getDescripcionEvento())
                        .fechaHora(b.getFechaHora())
                        .build())
                .toList();
    }

    private <T> Specification<T> fechaEntre(String campo, LocalDateTime desde, LocalDateTime hasta) {
        return (root, query, cb) -> {
            if (desde == null && hasta == null) return null;
            if (desde != null && hasta != null) return cb.between(root.get(campo), desde, hasta);
            if (desde != null) return cb.greaterThanOrEqualTo(root.get(campo), desde);
            return cb.lessThanOrEqualTo(root.get(campo), hasta);
        };
    }
}
