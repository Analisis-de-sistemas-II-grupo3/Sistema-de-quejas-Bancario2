package com.umg.quejasbancario.service;

import com.umg.quejasbancario.entity.*;
import com.umg.quejasbancario.entity.enums.TipoEventoAcceso;
import com.umg.quejasbancario.repository.BitacoraAccesoRepository;
import com.umg.quejasbancario.repository.BitacoraCasoRepository;
import com.umg.quejasbancario.repository.BitacoraCorreoRepository;
import com.umg.quejasbancario.repository.BitacoraUsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Centraliza la escritura en las 4 bitacoras inmutables del sistema (RN15).
 * Ningun servicio debe insertar directamente en estos repositorios: todos
 * pasan por aqui para garantizar que siempre se registre usuario, IP, fecha
 * y hora, tal como exige la regla transversal RN15 y el numeral 4 de
 * "Reglas de Negocio" (Requerimientos Suplementarios).
 *
 * A nivel de motor de datos, la inmutabilidad adicional (nadie puede
 * actualizar ni borrar estos registros, ni siquiera el Administrador) se
 * refuerza con triggers BEFORE UPDATE / BEFORE DELETE. Ver extensions.sql.
 */
@Service
@RequiredArgsConstructor
public class BitacoraRegistroService {

    private final BitacoraCasoRepository bitacoraCasoRepository;
    private final BitacoraAccesoRepository bitacoraAccesoRepository;
    private final BitacoraUsuarioRepository bitacoraUsuarioRepository;
    private final BitacoraCorreoRepository bitacoraCorreoRepository;

    @Transactional
    public void registrarEventoCaso(Caso caso, Usuario usuario, String rolEjecuta, String ip,
                                     String estadoAnterior, String estadoNuevo, String descripcion) {
        BitacoraCaso registro = BitacoraCaso.builder()
                .caso(caso)
                .usuario(usuario)
                .rolEjecuta(rolEjecuta)
                .ip(ip)
                .estadoAnterior(estadoAnterior)
                .estadoNuevo(estadoNuevo)
                .descripcionEvento(descripcion)
                .build();
        bitacoraCasoRepository.save(registro);
    }

    @Transactional
    public void registrarEventoAcceso(Usuario usuario, String ip, TipoEventoAcceso tipoEvento, String descripcion) {
        BitacoraAcceso registro = BitacoraAcceso.builder()
                .usuario(usuario)
                .ip(ip)
                .tipoEvento(tipoEvento)
                .descripcionEvento(descripcion)
                .build();
        bitacoraAccesoRepository.save(registro);
    }

    @Transactional
    public void registrarEventoUsuario(Usuario afectado, Usuario ejecuta, String motivo, String descripcion) {
        BitacoraUsuario registro = BitacoraUsuario.builder()
                .usuarioAfectado(afectado)
                .usuarioEjecuta(ejecuta)
                .motivo(motivo)
                .descripcionEvento(descripcion)
                .build();
        bitacoraUsuarioRepository.save(registro);
    }

    @Transactional
    public void registrarEventoCorreo(Caso caso, String destinatario, String tipoNotificacion, String descripcion) {
        BitacoraCorreo registro = BitacoraCorreo.builder()
                .caso(caso)
                .destinatario(destinatario)
                .tipoNotificacion(tipoNotificacion)
                .descripcionEvento(descripcion)
                .build();
        bitacoraCorreoRepository.save(registro);
    }
}
