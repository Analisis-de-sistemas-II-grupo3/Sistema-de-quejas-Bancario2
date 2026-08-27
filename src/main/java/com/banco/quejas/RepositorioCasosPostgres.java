package com.banco.quejas;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** Consultas PostgreSQL necesarias para CU02 y CU03. */
@Repository
public class RepositorioCasosPostgres {
    private final JdbcTemplate jdbc;
    public RepositorioCasosPostgres(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<ProductoServicio> catalogo() {
        return jdbc.query("SELECT id_producto,nombre FROM producto_servicio ORDER BY nombre", (rs, fila) ->
                new ProductoServicio(String.valueOf(rs.getLong(1)), rs.getString(2)));
    }
    public boolean existeProducto(String id) {
        try { return jdbc.queryForObject("SELECT COUNT(*) FROM producto_servicio WHERE id_producto=?", Integer.class, Long.parseLong(id)) > 0; }
        catch (NumberFormatException error) { return false; }
    }

    @Transactional
    public Caso guardarCaso(Cliente cliente, SesionUsuario sesion, SolicitudRegistroCaso solicitud) {
        long clienteId = Long.parseLong(cliente.id());
        long productoId = Long.parseLong(solicitud.productoId());
        Long cuentaId = jdbc.query("SELECT id_cuenta FROM cuenta_bancaria WHERE id_usuario=? AND estado=TRUE LIMIT 1", (rs, fila) -> rs.getLong(1), clienteId).stream().findFirst().orElse(null);
        if (cuentaId == null) throw new RegistroCasoException("El cliente debe tener una cuenta activa.");
        Long tipoId = jdbc.queryForObject("SELECT id_tipo_caso FROM tipo_caso WHERE LOWER(nombre)=LOWER(?)", Long.class, nombreTipo(solicitud.tipo()));
        Long categoriaId = categoriaParaProducto(productoId);
        Long agenteId = jdbc.query("SELECT u.id_usuario FROM usuario u JOIN rol r ON r.id_rol=u.id_rol WHERE r.nombre_rol='AGENTE' AND u.estado=TRUE ORDER BY RANDOM() LIMIT 1", (rs, fila) -> rs.getLong(1)).stream().findFirst().orElse(null);
        LocalDateTime ahora = LocalDateTime.now();
        String folio = siguienteFolio(solicitud.tipo(), ahora.getYear());
        String estado = agenteId == null ? "RECIBIDO" : "ASIGNADO";
        Long casoId = jdbc.queryForObject("INSERT INTO caso(descripcion,estado,fecha_registro,numero_caso,id_agente_asignado,id_categoria,id_cliente,id_cuenta,id_producto,id_tipo_caso,sucursal_canal,fecha_hecho) VALUES(?,?,?,?,?,?,?,?,?,?,?,?) RETURNING id_caso",
                Long.class, solicitud.descripcion().trim(), estado, Timestamp.valueOf(ahora), folio, agenteId, categoriaId, clienteId, cuentaId, productoId, tipoId, solicitud.sucursalCanal().trim(), Date.valueOf(solicitud.fechaHecho()));
        jdbc.update("INSERT INTO bitacora_caso(descripcion_evento,estado_anterior,estado_nuevo,fecha_hora,ip,rol_ejecuta,id_caso,id_usuario) VALUES(?,?,?,?,?,?,?,?)",
                "CREACIÓN DE CASO: " + folio, null, "RECIBIDO", Timestamp.valueOf(ahora), sesion.ip(), sesion.rol(), casoId, clienteId);
        ProductoServicio producto = jdbc.queryForObject("SELECT id_producto,nombre FROM producto_servicio WHERE id_producto=?", (rs, fila) -> new ProductoServicio(String.valueOf(rs.getLong(1)),rs.getString(2)), productoId);
        AgenteAtencion agente = agenteId == null ? null : new AgenteAtencion(String.valueOf(agenteId), "Agente asignado", true, 0, 10);
        return new Caso(folio, cliente.id(), cliente.nombreCompleto(), solicitud.tipo(), producto, solicitud.sucursalCanal(), solicitud.fechaHecho(), solicitud.descripcion(), solicitud.adjunto(), EstadoCaso.valueOf(estado), agente, ahora);
    }
    public java.util.Optional<Caso> buscar(String folio) {
        return jdbc.query("SELECT c.numero_caso,u.id_usuario,u.nombre_completo,tc.nombre,p.id_producto,p.nombre,c.sucursal_canal,c.fecha_hecho,c.descripcion,c.estado,c.fecha_registro FROM caso c JOIN usuario u ON u.id_usuario=c.id_cliente JOIN tipo_caso tc ON tc.id_tipo_caso=c.id_tipo_caso JOIN producto_servicio p ON p.id_producto=c.id_producto WHERE c.numero_caso=?", (rs,fila) -> new Caso(rs.getString(1),String.valueOf(rs.getLong(2)),rs.getString(3),TipoCaso.valueOf(rs.getString(4).toUpperCase()),new ProductoServicio(String.valueOf(rs.getLong(5)),rs.getString(6)),rs.getString(7),rs.getDate(8).toLocalDate(),rs.getString(9),null,EstadoCaso.valueOf(rs.getString(10)),null,rs.getTimestamp(11).toLocalDateTime()),folio).stream().findFirst();
    }
    @Transactional
    public void guardarDocumento(String folio, EvidenciaCaso evidencia, SesionUsuario sesion) {
        guardarDocumentos(folio, List.of(evidencia), sesion);
    }
    /** Una carga múltiple se confirma completa o se revierte completa. */
    @Transactional
    public void guardarDocumentos(String folio, List<EvidenciaCaso> evidencias, SesionUsuario sesion) {
        for (EvidenciaCaso evidencia : evidencias) guardarDocumentoInterno(folio, evidencia, sesion);
    }
    private void guardarDocumentoInterno(String folio, EvidenciaCaso evidencia, SesionUsuario sesion) {
        Long casoId = jdbc.queryForObject("SELECT id_caso FROM caso WHERE numero_caso=?", Long.class, folio);
        Long usuarioId = jdbc.queryForObject("SELECT id_usuario FROM usuario WHERE nombre_usuario=?", Long.class, sesion.usuario());
        byte[] contenido;
        try {
            contenido = Files.readAllBytes(evidencia.rutaGuardada());
        } catch (IOException error) {
            throw new ErrorEvidenciaException("No fue posible leer el archivo para guardarlo en la base de datos.");
        }
        jdbc.update("INSERT INTO documento_adjunto(fecha_carga,nombre_archivo,ruta_archivo,tamano,id_caso,id_usuario_carga,tipo_contenido,contenido) VALUES(?,?,?,?,?,?,?,?)",
                Timestamp.valueOf(evidencia.fechaCarga()), evidencia.nombreOriginal(), evidencia.rutaGuardada().toString(),
                evidencia.tamanioBytes(), casoId, usuarioId, tipoContenido(evidencia.extension()), contenido);
        jdbc.update("INSERT INTO bitacora_caso(descripcion_evento,estado_anterior,estado_nuevo,fecha_hora,ip,rol_ejecuta,id_caso,id_usuario) VALUES(?,?,?,?,?,?,?,?)",
                "CARGA DE EVIDENCIA: " + evidencia.nombreOriginal(), null, null, Timestamp.valueOf(evidencia.fechaCarga()),
                sesion.ip(), sesion.rol(), casoId, usuarioId);
    }
    public List<EvidenciaCaso> documentos(String folio) {
        return jdbc.query("SELECT d.id_documento,d.nombre_archivo,d.ruta_archivo,d.tamano,d.fecha_carga,u.nombre_usuario FROM documento_adjunto d JOIN caso c ON c.id_caso=d.id_caso JOIN usuario u ON u.id_usuario=d.id_usuario_carga WHERE c.numero_caso=? ORDER BY d.fecha_carga DESC", (rs,fila) -> {
            String nombre=rs.getString(2); int punto=nombre.lastIndexOf('.'); String extension=punto<0?"":nombre.substring(punto+1);
            return new EvidenciaCaso(rs.getLong(1), nombre, Path.of(rs.getString(3)),rs.getLong(4),extension,rs.getString(6),rs.getTimestamp(5).toLocalDateTime());
        }, folio);
    }
    public ArchivoEvidencia obtenerArchivo(String folio, long documentoId) {
        return jdbc.query("SELECT d.nombre_archivo,d.tipo_contenido,d.contenido FROM documento_adjunto d JOIN caso c ON c.id_caso=d.id_caso WHERE c.numero_caso=? AND d.id_documento=?", (rs, fila) ->
                new ArchivoEvidencia(rs.getString(1), rs.getString(2), rs.getBytes(3)), folio, documentoId)
                .stream().findFirst().orElseThrow(() -> new ErrorEvidenciaException("No se encontró la evidencia solicitada."));
    }
    private Long categoriaParaProducto(long productoId) { return jdbc.queryForObject("SELECT id_categoria FROM producto_servicio WHERE id_producto=?",Long.class, productoId); }
    private String siguienteFolio(TipoCaso tipo,int anio) { Integer n=jdbc.queryForObject("SELECT COUNT(*)+1 FROM caso WHERE numero_caso LIKE ?",Integer.class,tipo.prefijo()+"-%-"+anio); return "%s-%05d-%d".formatted(tipo.prefijo(),n,anio); }
    private String nombreTipo(TipoCaso tipo) { return tipo.name().substring(0,1)+tipo.name().substring(1).toLowerCase(); }
    private String tipoContenido(String extension) {
        return switch (extension.toLowerCase()) {
            case "pdf" -> "application/pdf";
            case "png" -> "image/png";
            default -> "image/jpeg";
        };
    }
}
