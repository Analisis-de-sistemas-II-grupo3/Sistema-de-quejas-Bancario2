package com.umg.quejasbancario.service;

import com.umg.quejasbancario.config.AppProperties;
import com.umg.quejasbancario.dto.response.CasoPublicoResponse;
import com.umg.quejasbancario.dto.response.InstitucionResponse;
import com.umg.quejasbancario.entity.BitacoraCaso;
import com.umg.quejasbancario.entity.Caso;
import com.umg.quejasbancario.exception.ResourceNotFoundException;
import com.umg.quejasbancario.repository.BitacoraCasoRepository;
import com.umg.quejasbancario.repository.CasoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * CU-00 Portal / Pagina de Inicio: servicios publicos (sin sesion).
 *  - Identidad institucional configurable (RN17).
 *  - Consulta del estado de un caso sin iniciar sesion (FA01, CU-03).
 */
@Service
@RequiredArgsConstructor
public class PortalService {

    /** RN05: PREFIJO-correlativo de 5 digitos-anio, p.ej. Q-00001-2026. */
    private static final Pattern FORMATO_NUMERO_CASO = Pattern.compile("^[QRDS]-\\d{5}-\\d{4}$");

    private static final String MSG_CASO_NO_EXISTE =
            "Por favor verifique, el número de caso no existe en el sistema.";

    private final AppProperties appProperties;
    private final CasoRepository casoRepository;
    private final BitacoraCasoRepository bitacoraCasoRepository;

    /** Identidad institucional de la instancia, leida de la configuracion (RN17). */
    public InstitucionResponse obtenerInstitucion() {
        AppProperties.Institucion cfg = appProperties.getInstitucion();
        return InstitucionResponse.builder()
                .nombre(cfg.getNombre())
                .eslogan(cfg.getEslogan())
                .logoUrl(cfg.getLogoUrl())
                .colores(InstitucionResponse.Colores.builder()
                        .primario(cfg.getColorPrimario())
                        .secundario(cfg.getColorSecundario())
                        .acento(cfg.getColorAcento())
                        .build())
                .mision(cfg.getMision())
                .vision(cfg.getVision())
                .valores(cfg.getValores().stream()
                        .map(v -> InstitucionResponse.Valor.builder()
                                .nombre(v.getNombre())
                                .descripcion(v.getDescripcion())
                                .build())
                        .toList())
                .build();
    }

    /**
     * Estado publico de un caso a partir de su numero. Un formato invalido y un
     * numero inexistente responden igual (AN02 #5) para no dar pistas.
     */
    @Transactional(readOnly = true)
    public CasoPublicoResponse consultarEstadoCaso(String numeroCaso) {
        String numero = numeroCaso == null ? "" : numeroCaso.trim().toUpperCase(Locale.ROOT);
        if (!FORMATO_NUMERO_CASO.matcher(numero).matches()) {
            throw new ResourceNotFoundException(MSG_CASO_NO_EXISTE);
        }

        Caso caso = casoRepository.findByNumeroCaso(numero)
                .orElseThrow(() -> new ResourceNotFoundException(MSG_CASO_NO_EXISTE));

        // Solo transiciones reales de estado (RN08); sin usuario, rol, IP ni descripcion.
        List<CasoPublicoResponse.Etapa> etapas = bitacoraCasoRepository
                .findByCaso_IdCasoOrderByFechaHoraAsc(caso.getIdCaso()).stream()
                .filter(b -> !Objects.equals(b.getEstadoAnterior(), b.getEstadoNuevo()))
                .map(this::mapearEtapa)
                .toList();

        LocalDateTime ultimaActualizacion = etapas.isEmpty()
                ? caso.getFechaRegistro()
                : etapas.get(etapas.size() - 1).getFechaHora();

        return CasoPublicoResponse.builder()
                .numeroCaso(caso.getNumeroCaso())
                .tipoCaso(caso.getTipoCaso().getNombre())
                .estado(caso.getEstado().getValor())
                .fechaRegistro(caso.getFechaRegistro())
                .fechaUltimaActualizacion(ultimaActualizacion)
                .fechaCierre(caso.getFechaCierre())
                .etapas(etapas)
                .build();
    }

    private CasoPublicoResponse.Etapa mapearEtapa(BitacoraCaso b) {
        return CasoPublicoResponse.Etapa.builder()
                .estadoAnterior(b.getEstadoAnterior())
                .estadoNuevo(b.getEstadoNuevo())
                .fechaHora(b.getFechaHora())
                .build();
    }
}
