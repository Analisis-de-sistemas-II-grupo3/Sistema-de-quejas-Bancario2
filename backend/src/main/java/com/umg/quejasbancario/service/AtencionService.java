package com.umg.quejasbancario.service;

import com.umg.quejasbancario.entity.Caso;
import com.umg.quejasbancario.entity.Usuario;
import com.umg.quejasbancario.entity.enums.EstadoCaso;
import com.umg.quejasbancario.entity.enums.RolNombre;
import com.umg.quejasbancario.exception.AccesoDenegadoException;
import com.umg.quejasbancario.exception.BusinessRuleException;
import com.umg.quejasbancario.exception.ResourceNotFoundException;
import com.umg.quejasbancario.repository.CasoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * CU-07 Iniciar Atencion de Caso, CU-08 Resolver Caso, CU-09 Cerrar Caso.
 * Tambien cubre las transiciones auxiliares "poner en espera" / "reanudar"
 * mencionadas en RN08 (ciclo de vida del caso).
 */
@Service
@RequiredArgsConstructor
public class AtencionService {

    private final CasoRepository casoRepository;
    private final BitacoraRegistroService bitacoraRegistroService;
    private final NotificacionService notificacionService;

    /** CU-07 Iniciar Atencion de Caso. */
    @Transactional
    public void iniciarAtencion(Integer idCaso, Usuario agente, String ip) {
        Caso caso = obtenerCasoDelAgente(idCaso, agente);

        if (caso.getEstado() != EstadoCaso.ASIGNADO && caso.getEstado() != EstadoCaso.EN_ESPERA) {
            throw new BusinessRuleException("El caso no se encuentra en un estado válido para iniciar su atención.");
        }

        String estadoAnterior = caso.getEstado().getValor();
        caso.setEstado(EstadoCaso.EN_ATENCION);
        casoRepository.save(caso);

        bitacoraRegistroService.registrarEventoCaso(caso, agente, RolNombre.AGENTE.getValor(), ip,
                estadoAnterior, EstadoCaso.EN_ATENCION.getValor(),
                "El Agente '" + agente.getNombreCompleto() + "' inició la atención del caso.");

        notificacionService.notificarCambioEstado(caso, caso.getCorreoContacto(), estadoAnterior, EstadoCaso.EN_ATENCION.getValor());
    }

    /** Poner el caso "En espera" (p.ej. mientras se espera informacion adicional del cliente). */
    @Transactional
    public void ponerEnEspera(Integer idCaso, Usuario agente, String motivo, String ip) {
        Caso caso = obtenerCasoDelAgente(idCaso, agente);

        if (caso.getEstado() != EstadoCaso.EN_ATENCION) {
            throw new BusinessRuleException("Solo se puede poner en espera un caso que se encuentra En atención.");
        }

        String estadoAnterior = caso.getEstado().getValor();
        caso.setEstado(EstadoCaso.EN_ESPERA);
        casoRepository.save(caso);

        String descripcion = "El Agente '" + agente.getNombreCompleto() + "' puso el caso en espera."
                + (motivo != null && !motivo.isBlank() ? " Motivo: " + motivo : "");
        bitacoraRegistroService.registrarEventoCaso(caso, agente, RolNombre.AGENTE.getValor(), ip,
                estadoAnterior, EstadoCaso.EN_ESPERA.getValor(), descripcion);

        notificacionService.notificarCambioEstado(caso, caso.getCorreoContacto(), estadoAnterior, EstadoCaso.EN_ESPERA.getValor());
    }

    /** CU-08 Resolver Caso. */
    @Transactional
    public void resolverCaso(Integer idCaso, Usuario agente, String detalleResolucion, String ip) {
        Caso caso = obtenerCasoDelAgente(idCaso, agente);

        if (caso.getEstado() != EstadoCaso.EN_ATENCION && caso.getEstado() != EstadoCaso.EN_ESPERA) {
            throw new BusinessRuleException("El caso debe encontrarse En atención o En espera para poder resolverse.");
        }
        if (detalleResolucion == null || detalleResolucion.isBlank()) {
            throw new BusinessRuleException("Por favor ingrese los campos obligatorios.");
        }

        String estadoAnterior = caso.getEstado().getValor();
        caso.setEstado(EstadoCaso.RESUELTO);
        caso.setDetalleResolucion(detalleResolucion);
        casoRepository.save(caso);

        bitacoraRegistroService.registrarEventoCaso(caso, agente, RolNombre.AGENTE.getValor(), ip,
                estadoAnterior, EstadoCaso.RESUELTO.getValor(),
                "El Agente '" + agente.getNombreCompleto() + "' registró la resolución del caso.");

        notificacionService.notificarCasoResuelto(caso, caso.getCorreoContacto());
    }

    /** CU-09 Cerrar Caso. Puede ejecutarlo el Agente responsable o el Administrador/Supervisor. */
    @Transactional
    public void cerrarCaso(Integer idCaso, Usuario usuarioEjecuta, String ip) {
        Caso caso = casoRepository.findById(idCaso)
                .orElseThrow(() -> new ResourceNotFoundException("Por favor verifique, el número de caso no existe en el sistema."));

        String rol = usuarioEjecuta.getRol().getNombreRol();
        boolean esAgenteResponsable = caso.getAgenteAsignado() != null
                && caso.getAgenteAsignado().getIdUsuario().equals(usuarioEjecuta.getIdUsuario());
        boolean esSupervisorOAdmin = RolNombre.SUPERVISOR.getValor().equalsIgnoreCase(rol)
                || RolNombre.ADMINISTRADOR.getValor().equalsIgnoreCase(rol);

        if (!esAgenteResponsable && !esSupervisorOAdmin) {
            throw new AccesoDenegadoException("No tiene autorización para cerrar este caso.");
        }

        if (caso.getEstado() != EstadoCaso.RESUELTO) {
            throw new BusinessRuleException("Solo se pueden cerrar casos que se encuentren en estado Resuelto.");
        }

        String estadoAnterior = caso.getEstado().getValor();
        caso.setEstado(EstadoCaso.CERRADO);
        caso.setFechaCierre(LocalDateTime.now());
        casoRepository.save(caso);

        bitacoraRegistroService.registrarEventoCaso(caso, usuarioEjecuta, rol, ip,
                estadoAnterior, EstadoCaso.CERRADO.getValor(),
                "'" + usuarioEjecuta.getNombreCompleto() + "' cerró formalmente el caso.");

        notificacionService.notificarCasoCerrado(caso, caso.getCorreoContacto());
    }

    private Caso obtenerCasoDelAgente(Integer idCaso, Usuario agente) {
        Caso caso = casoRepository.findById(idCaso)
                .orElseThrow(() -> new ResourceNotFoundException("Por favor verifique, el número de caso no existe en el sistema."));

        if (caso.getAgenteAsignado() == null || !caso.getAgenteAsignado().getIdUsuario().equals(agente.getIdUsuario())) {
            throw new AccesoDenegadoException("No tiene autorización para gestionar este caso.");
        }
        return caso;
    }
}
