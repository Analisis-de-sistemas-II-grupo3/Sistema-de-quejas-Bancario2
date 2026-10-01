package com.umg.quejasbancario.scheduler;

import com.umg.quejasbancario.entity.Caso;
import com.umg.quejasbancario.entity.enums.EstadoCaso;
import com.umg.quejasbancario.repository.CasoRepository;
import com.umg.quejasbancario.service.AsignacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * RF17: cuando no existen Agentes disponibles al momento de registrar un
 * caso (RN06/CU-04 FA01), el caso permanece en estado "Registrado" sin
 * agente asignado. Este proceso reintenta periodicamente la asignacion
 * automatica hasta que haya un Agente disponible.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AsignacionScheduler {

    private static final String IP_SISTEMA = "127.0.0.1 (proceso automático)";

    private final CasoRepository casoRepository;
    private final AsignacionService asignacionService;

    /** Se ejecuta cada 2 minutos. Ajustable segun necesidad del negocio. */
    @Scheduled(fixedDelayString = "${app.scheduler.reintento-asignacion-ms:120000}")
    public void reintentarAsignacionPendientes() {
        List<Caso> pendientes = casoRepository.findByEstadoAndAgenteAsignadoIsNull(EstadoCaso.REGISTRADO);
        if (pendientes.isEmpty()) return;

        log.info("Reintentando asignación automática para {} caso(s) sin agente disponible.", pendientes.size());
        for (Caso caso : pendientes) {
            asignacionService.asignarCaso(caso, null, IP_SISTEMA);
        }
    }
}
