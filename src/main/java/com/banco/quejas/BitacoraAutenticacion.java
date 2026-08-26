package com.banco.quejas;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import java.sql.Timestamp;

/** Bitácora en memoria de eventos de acceso (inicio, cierre manual, cierre automático). */
@Service
public class BitacoraAutenticacion {
    private final List<EventoBitacoraAcceso> eventos = new CopyOnWriteArrayList<>();
    private final JdbcTemplate jdbc;

    /** Se conserva para que las pruebas sencillas de POO sigan funcionando. */
    public BitacoraAutenticacion() { this.jdbc = null; }
    @Autowired
    public BitacoraAutenticacion(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public void registrar(String evento, String usuario, String rol, String ip, String descripcion) {
        LocalDateTime ahora = LocalDateTime.now();
        eventos.add(new EventoBitacoraAcceso(ahora, evento, usuario, rol, ip, descripcion));
        if (jdbc != null) {
            List<Long> ids = jdbc.query("SELECT id_usuario FROM usuario WHERE nombre_usuario=?", (rs, fila) -> rs.getLong(1), usuario);
            if (!ids.isEmpty()) jdbc.update("INSERT INTO bitacora_acceso(descripcion_evento,fecha_hora,ip,tipo_evento,id_usuario) VALUES(?,?,?,?,?)",
                    descripcion, Timestamp.valueOf(ahora), ip, evento, ids.get(0));
        }
    }

    public List<EventoBitacoraAcceso> consultar() { return List.copyOf(eventos); }
}
