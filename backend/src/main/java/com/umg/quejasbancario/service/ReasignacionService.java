package com.umg.quejasbancario.service;

import com.umg.quejasbancario.dto.response.SolicitudReasignacionResponse;
import com.umg.quejasbancario.entity.Caso;
import com.umg.quejasbancario.entity.SolicitudReasignacion;
import com.umg.quejasbancario.entity.Usuario;
import com.umg.quejasbancario.entity.enums.EstadoCaso;
import com.umg.quejasbancario.entity.enums.EstadoSolicitudReasignacion;
import com.umg.quejasbancario.entity.enums.RolNombre;
import com.umg.quejasbancario.exception.AccesoDenegadoException;
import com.umg.quejasbancario.exception.BusinessRuleException;
import com.umg.quejasbancario.exception.ResourceNotFoundException;
import com.umg.quejasbancario.repository.CasoRepository;
import com.umg.quejasbancario.repository.SolicitudReasignacionRepository;
import com.umg.quejasbancario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * CU-05 Solicitar Reasignacion de Caso, CU-06 Aprobar/Rechazar Solicitud de
 * Reasignacion. RN07: un caso admite un maximo de 2 solicitudes de
 * reasignacion durante su ciclo de vida.
 */
@Service
@RequiredArgsConstructor
public class ReasignacionService {

    private static final int MAX_SOLICITUDES_POR_CASO = 2;

    private final CasoRepository casoRepository;
    private final SolicitudReasignacionRepository solicitudReasignacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final AsignacionService asignacionService;
    private final BitacoraRegistroService bitacoraRegistroService;
    private final NotificacionService notificacionService;

    /** CU-05 Solicitar Reasignacion de Caso. */
    @Transactional
    public SolicitudReasignacionResponse solicitar(Integer idCaso, Usuario agente, String motivo, String ip) {
        Caso caso = casoRepository.findById(idCaso)
                .orElseThrow(() -> new ResourceNotFoundException("Por favor verifique, el número de caso no existe en el sistema."));

        if (caso.getAgenteAsignado() == null || !caso.getAgenteAsignado().getIdUsuario().equals(agente.getIdUsuario())) {
            throw new AccesoDenegadoException("No tiene autorización para solicitar la reasignación de este caso.");
        }

        if (caso.getEstado() == EstadoCaso.RESUELTO || caso.getEstado() == EstadoCaso.CERRADO) {
            throw new BusinessRuleException("No es posible solicitar la reasignación de un caso Resuelto o Cerrado.");
        }

        if (solicitudReasignacionRepository.findByCaso_IdCasoAndEstado(idCaso, EstadoSolicitudReasignacion.PENDIENTE).isPresent()) {
            throw new BusinessRuleException("Este caso ya tiene una solicitud de reasignación pendiente de aprobación.");
        }

        // RN07: maximo 2 solicitudes de reasignacion por caso.
        if (caso.getSolicitudesReasignacionUsadas() != null && caso.getSolicitudesReasignacionUsadas() >= MAX_SOLICITUDES_POR_CASO) {
            throw new BusinessRuleException("Este caso ya alcanzó el número máximo de solicitudes de reasignación permitidas.");
        }

        if (motivo == null || motivo.isBlank()) {
            throw new BusinessRuleException("Por favor ingrese los campos obligatorios.");
        }

        SolicitudReasignacion solicitud = SolicitudReasignacion.builder()
                .caso(caso)
                .agenteSolicita(agente)
                .motivo(motivo)
                .estado(EstadoSolicitudReasignacion.PENDIENTE)
                .build();
        solicitud = solicitudReasignacionRepository.save(solicitud);

        bitacoraRegistroService.registrarEventoCaso(caso, agente, RolNombre.AGENTE.getValor(), ip,
                caso.getEstado().getValor(), caso.getEstado().getValor(),
                "El Agente '" + agente.getNombreCompleto() + "' solicitó la reasignación del caso. Motivo: " + motivo);

        // Notificar a todos los Supervisores activos (Aviso #5).
        List<Usuario> supervisores = usuarioRepository.findByRol_NombreRolIgnoreCase(RolNombre.SUPERVISOR.getValor());
        for (Usuario supervisor : supervisores) {
            notificacionService.notificarSolicitudReasignacion(caso, supervisor.getCorreoElectronico(), agente.getNombreCompleto(), motivo);
        }

        return mapear(solicitud);
    }

    /** CU-06 - listado de solicitudes pendientes para el Supervisor. */
    @Transactional(readOnly = true)
    public List<SolicitudReasignacionResponse> listarPendientes() {
        return solicitudReasignacionRepository.findByEstado(EstadoSolicitudReasignacion.PENDIENTE).stream()
                .map(this::mapear).toList();
    }

    /** CU-06 - Aprobar. Dispara una nueva asignacion automatica excluyendo al Agente original. */
    @Transactional
    public SolicitudReasignacionResponse aprobar(Integer idSolicitud, Usuario supervisor, String ip) {
        SolicitudReasignacion solicitud = obtenerPendiente(idSolicitud);
        Caso caso = solicitud.getCaso();
        Usuario agenteOriginal = solicitud.getAgenteSolicita();

        solicitud.setEstado(EstadoSolicitudReasignacion.APROBADA);
        solicitud.setSupervisorResuelve(supervisor);
        solicitud.setFechaResolucion(LocalDateTime.now());
        solicitudReasignacionRepository.save(solicitud);

        caso.setSolicitudesReasignacionUsadas(
                (caso.getSolicitudesReasignacionUsadas() == null ? 0 : caso.getSolicitudesReasignacionUsadas()) + 1);
        caso.setAgenteAsignado(null);
        casoRepository.save(caso);

        bitacoraRegistroService.registrarEventoCaso(caso, supervisor, RolNombre.SUPERVISOR.getValor(), ip,
                caso.getEstado().getValor(), caso.getEstado().getValor(),
                "El Supervisor '" + supervisor.getNombreCompleto() + "' aprobó la solicitud de reasignación.");

        // RN06/CU-04: nueva asignacion aleatoria excluyendo al agente original.
        asignacionService.asignarCaso(caso, agenteOriginal, ip);

        notificacionService.notificarReasignacionAprobada(caso, agenteOriginal.getCorreoElectronico(), agenteOriginal.getNombreCompleto());

        return mapear(solicitud);
    }

    /** CU-06 - Rechazar (FA01: se ingresa motivo de rechazo, el caso permanece con el mismo Agente). */
    @Transactional
    public SolicitudReasignacionResponse rechazar(Integer idSolicitud, Usuario supervisor, String motivoRechazo, String ip) {
        SolicitudReasignacion solicitud = obtenerPendiente(idSolicitud);
        Caso caso = solicitud.getCaso();

        solicitud.setEstado(EstadoSolicitudReasignacion.RECHAZADA);
        solicitud.setSupervisorResuelve(supervisor);
        solicitud.setMotivoRechazo(motivoRechazo);
        solicitud.setFechaResolucion(LocalDateTime.now());
        solicitudReasignacionRepository.save(solicitud);

        bitacoraRegistroService.registrarEventoCaso(caso, supervisor, RolNombre.SUPERVISOR.getValor(), ip,
                caso.getEstado().getValor(), caso.getEstado().getValor(),
                "El Supervisor '" + supervisor.getNombreCompleto() + "' rechazó la solicitud de reasignación."
                        + (motivoRechazo != null && !motivoRechazo.isBlank() ? " Motivo: " + motivoRechazo : ""));

        return mapear(solicitud);
    }

    private SolicitudReasignacion obtenerPendiente(Integer idSolicitud) {
        SolicitudReasignacion solicitud = solicitudReasignacionRepository.findById(idSolicitud)
                .orElseThrow(() -> new ResourceNotFoundException("La solicitud de reasignación no existe."));
        if (solicitud.getEstado() != EstadoSolicitudReasignacion.PENDIENTE) {
            throw new BusinessRuleException("Esta solicitud ya fue resuelta anteriormente.");
        }
        return solicitud;
    }

    private SolicitudReasignacionResponse mapear(SolicitudReasignacion s) {
        return SolicitudReasignacionResponse.builder()
                .idSolicitud(s.getIdSolicitud())
                .numeroCaso(s.getCaso().getNumeroCaso())
                .agenteSolicita(s.getAgenteSolicita().getNombreCompleto())
                .supervisorResuelve(s.getSupervisorResuelve() != null ? s.getSupervisorResuelve().getNombreCompleto() : null)
                .motivo(s.getMotivo())
                .motivoRechazo(s.getMotivoRechazo())
                .estado(s.getEstado().getValor())
                .fechaSolicitud(s.getFechaSolicitud())
                .fechaResolucion(s.getFechaResolucion())
                .build();
    }
}
