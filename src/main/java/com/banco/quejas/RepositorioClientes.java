package com.banco.quejas;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.jdbc.core.JdbcTemplate;
/** Repositorio en memoria de clientes bancarios. La base de datos lo sustituirá (ver README). */
public class RepositorioClientes {
    private final List<Cliente> clientes;
    private final JdbcTemplate jdbc;
    public RepositorioClientes(List<Cliente> clientesIniciales) {
        this.clientes = new CopyOnWriteArrayList<>(clientesIniciales);
        this.jdbc = null;
    }
    public RepositorioClientes(JdbcTemplate jdbc) { this.clientes = null; this.jdbc = jdbc; }
    public Optional<Cliente> buscarPorId(String id) {
        if (id == null) return Optional.empty();
        if (jdbc != null) {
            return jdbc.query("SELECT id_usuario, nombre_completo, correo_electronico, dpi_nit FROM usuario WHERE id_usuario=?", (rs, fila) -> {
                long usuarioId = rs.getLong("id_usuario");
                List<CuentaBancaria> cuentas = jdbc.query("SELECT numero_cuenta, estado FROM cuenta_bancaria WHERE id_usuario=?", (cuenta, indice) ->
                        new CuentaBancaria(cuenta.getString("numero_cuenta"), cuenta.getBoolean("estado")), usuarioId);
                String dpiNit = rs.getString("dpi_nit");
                return new Cliente(String.valueOf(usuarioId), rs.getString("nombre_completo"), dpiNit == null ? "" : dpiNit, rs.getString("correo_electronico"), "", cuentas);
            }, Long.parseLong(id)).stream().findFirst();
        }
        return clientes.stream().filter(c -> c.id().equals(id)).findFirst();
    }
}