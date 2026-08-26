package com.banco.quejas;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.jdbc.core.JdbcTemplate;

/** Repositorio en memoria de cuentas de acceso. La base de datos lo sustituirá (ver README). */
public class RepositorioUsuarios {
    private final List<Usuario> usuarios;
    private final JdbcTemplate jdbc;

    public RepositorioUsuarios(List<Usuario> usuariosIniciales) {
        this.usuarios = new CopyOnWriteArrayList<>(usuariosIniciales);
        this.jdbc = null;
    }

    /** Constructor usado por la aplicación: consulta usuarios reales en PostgreSQL. */
    public RepositorioUsuarios(JdbcTemplate jdbc) { this.usuarios = null; this.jdbc = jdbc; }

    public Optional<Usuario> buscarPorUsuario(String usuario) {
        if (usuario == null || usuario.isBlank()) return Optional.empty();
        String buscado = usuario.trim();
        if (jdbc != null) {
            return jdbc.query("SELECT u.id_usuario, u.nombre_usuario, u.contrasena_hash, u.nombre_completo, u.estado, r.nombre_rol "
                    + "FROM usuario u JOIN rol r ON r.id_rol=u.id_rol WHERE LOWER(u.nombre_usuario)=LOWER(?)", (rs, fila) -> {
                String rol = RolesAplicacion.convertir(rs.getString("nombre_rol"));
                return new Usuario(String.valueOf(rs.getLong("id_usuario")), rs.getString("nombre_usuario"), rs.getString("contrasena_hash"),
                        rol, rs.getString("nombre_completo"), "Cliente".equals(rol) ? String.valueOf(rs.getLong("id_usuario")) : null, rs.getBoolean("estado"));
            }, buscado).stream().findFirst();
        }
        return usuarios.stream().filter(u -> u.usuario().equalsIgnoreCase(buscado)).findFirst();
    }
}
