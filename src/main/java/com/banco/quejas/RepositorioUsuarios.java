package com.banco.quejas;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/** Repositorio en memoria de cuentas de acceso. La base de datos lo sustituirá (ver README). */
public class RepositorioUsuarios {
    private final List<Usuario> usuarios;

    public RepositorioUsuarios(List<Usuario> usuariosIniciales) {
        this.usuarios = new CopyOnWriteArrayList<>(usuariosIniciales);
    }

    public Optional<Usuario> buscarPorUsuario(String usuario) {
        if (usuario == null || usuario.isBlank()) return Optional.empty();
        String buscado = usuario.trim();
        return usuarios.stream().filter(u -> u.usuario().equalsIgnoreCase(buscado)).findFirst();
    }
}
