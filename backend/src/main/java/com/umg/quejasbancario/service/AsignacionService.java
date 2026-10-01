package com.umg.quejasbancario.service;

import com.umg.quejasbancario.entity.Caso;
import com.umg.quejasbancario.entity.ParametroSistema;
import com.umg.quejasbancario.entity.Usuario;
import com.umg.quejasbancario.entity.enums.EstadoCaso;
import com.umg.quejasbancario.entity.enums.EstadoUsuario;
import com.umg.quejasbancario.entity.enums.RolNombre;
import com.umg.quejasbancario.repository.CasoRepository;
import com.umg.quejasbancario.repository.ParametroSistemaRepository;
import com.umg.quejasbancario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * CU-04 Asignar Caso Automaticamente. Implementa el criterio especial de
 * asignacion aleatoria uniforme definido en RN06, sin considerar
 * antiguedad ni severidad del caso. Se dispara desde dos lugares:
 *   - CasoService al registrar un caso nuevo (transicion Registrado -> Asignado).
 *   - ReasignacionService al aprobarse una solicitud (excluyendo al Agente original).
 * Si no hay Agentes disponibles, el caso permanece en su estado y
 * AsignacionScheduler reintenta periodicamente (RF17, FA01).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AsignacionService {

    private static final String PARAM_LIMITE_CASOS = "limite_casos_activos_agente";
    private static final int LIMITE_POR_DEFECTO = 10;

    private final UsuarioRepository usuarioRepository;
    private final CasoRepository casoRepository;
    private final ParametroSistemaRepository parametroSistemaRepository;
    private final BitacoraRegistroService bitacoraRegistroService;
    private final NotificacionService notificacionService;
    private final SecureRandom random = new SecureRandom();

    /**
     * Intenta asignar el caso a un Agente disponible, excluyendo opcionalmente
     * al agente original (cuando el disparador es una reasignacion aprobada,
     * RN07/FA02). Devuelve el Agente asignado, o vacio si no hay disponibles
     * (el caso permanece en su estado actual, RF17).
     */
    @Transactional
    public Optional<Usuario> asignarCaso(Caso caso, Usuario agenteExcluido, String ipSistema) {
        int limite = obtenerLimiteCasosActivos();

        List<Usuario> agentes = usuarioRepository.findByRol_NombreRolIgnoreCaseAndEstado(
                RolNombre.AGENTE.getValor(), EstadoUsuario.ACTIVO);

        List<Usuario> disponibles = agentes.stream()
                .filter(a -> agenteExcluido == null || !a.getIdUsuario().equals(agenteExcluido.getIdUsuario()))
                .filter(Usuario::getSesionActiva)
                .filter(a -> casoRepository.contarCasosActivosPorAgente(a.getIdUsuario()) < limite)
                .collect(Collectors.toList());

        if (disponibles.isEmpty()) {
            log.info("No hay Agentes disponibles para el caso {}. Permanece en estado {} para reintento periodico (RF17).",
                    caso.getNumeroCaso(), caso.getEstado());
            return Optional.empty();
        }

        // Seleccion aleatoria uniforme (RN06 RF15), sin considerar antiguedad ni severidad.
        Usuario seleccionado = disponibles.get(random.nextInt(disponibles.size()));

        String estadoAnterior = caso.getEstado().getValor();
        caso.setAgenteAsignado(seleccionado);
        caso.setEstado(EstadoCaso.ASIGNADO);
        casoRepository.save(caso);

        bitacoraRegistroService.registrarEventoCaso(
                caso, null, "Proceso automático", ipSistema,
                estadoAnterior, EstadoCaso.ASIGNADO.getValor(),
                "El Sistema asignó automáticamente el caso al agente '" + seleccionado.getNombreCompleto() + "' mediante selección aleatoria uniforme.");

        notificacionService.notificarNuevoCasoAsignado(caso, seleccionado.getCorreoElectronico());

        return Optional.of(seleccionado);
    }

    private int obtenerLimiteCasosActivos() {
        return parametroSistemaRepository.findByNombreParametroIgnoreCase(PARAM_LIMITE_CASOS)
                .map(ParametroSistema::getValor)
                .map(v -> {
                    try {
                        return Integer.parseInt(v.trim());
                    } catch (NumberFormatException e) {
                        return LIMITE_POR_DEFECTO;
                    }
                })
                .orElse(LIMITE_POR_DEFECTO);
    }
}
