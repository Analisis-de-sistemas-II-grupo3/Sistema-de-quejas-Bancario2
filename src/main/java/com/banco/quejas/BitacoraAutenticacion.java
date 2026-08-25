package com.banco.quejas;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Service;

/** Bitácora en memoria de eventos de acceso (inicio, cierre manual, cierre automático). */
@Service
public class BitacoraAutenticacion {
    private final List<EventoBitacoraAcceso> eventos = new CopyOnWriteArrayList<>();

    public void registrar(String evento, String usuario, String rol, String ip, String descripcion) {
        eventos.add(new EventoBitacoraAcceso(LocalDateTime.now(), evento, usuario, rol, ip, descripcion));
    }

    public List<EventoBitacoraAcceso> consultar() { return List.copyOf(eventos); }
}
