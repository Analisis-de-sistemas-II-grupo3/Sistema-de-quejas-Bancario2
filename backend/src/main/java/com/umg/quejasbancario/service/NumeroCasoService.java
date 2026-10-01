package com.umg.quejasbancario.service;

import com.umg.quejasbancario.entity.TipoCaso;
import com.umg.quejasbancario.repository.CasoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Year;

/**
 * RN05 - Numero de Caso: PREFIJO-Correlativo(5 digitos)-Anio, ej. Q-00001-2026.
 * El correlativo se reinicia cada anio y depende del tipo de caso (prefijo).
 *
 * Nota: para produccion con alta concurrencia se recomienda una secuencia
 * de base de datos por tipo/anio; aqui se calcula contando los casos ya
 * registrados en el anio en curso, protegido por 'synchronized' a nivel
 * de instancia para evitar colisiones dentro de un mismo proceso.
 */
@Service
@RequiredArgsConstructor
public class NumeroCasoService {

    private final CasoRepository casoRepository;

    public synchronized String generarNumeroCaso(TipoCaso tipoCaso) {
        int anioActual = Year.now().getValue();
        LocalDateTime inicioAnio = LocalDateTime.of(anioActual, 1, 1, 0, 0, 0);
        LocalDateTime finAnio = LocalDateTime.of(anioActual + 1, 1, 1, 0, 0, 0);

        long cantidadExistente = casoRepository.contarCasosPorTipoYAnio(tipoCaso.getIdTipoCaso(), inicioAnio, finAnio);
        long siguienteCorrelativo = cantidadExistente + 1;

        String correlativoFormateado = String.format("%05d", siguienteCorrelativo);
        String candidato = tipoCaso.getPrefijo() + "-" + correlativoFormateado + "-" + anioActual;

        // Salvaguarda ante colisiones (p.ej. multiples instancias): si ya existe, avanza el correlativo.
        while (casoRepository.findByNumeroCaso(candidato).isPresent()) {
            siguienteCorrelativo++;
            correlativoFormateado = String.format("%05d", siguienteCorrelativo);
            candidato = tipoCaso.getPrefijo() + "-" + correlativoFormateado + "-" + anioActual;
        }

        return candidato;
    }
}
