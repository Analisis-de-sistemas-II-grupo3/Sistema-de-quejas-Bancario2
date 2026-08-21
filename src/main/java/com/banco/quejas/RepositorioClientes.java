package com.banco.quejas;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/** Repositorio en memoria de clientes bancarios. La base de datos lo sustituirá (ver README). */
public class RepositorioClientes {
    private final List<Cliente> clientes;

    public RepositorioClientes(List<Cliente> clientesIniciales) {
        this.clientes = new CopyOnWriteArrayList<>(clientesIniciales);
    }

    public Optional<Cliente> buscarPorId(String id) {
        if (id == null) return Optional.empty();
        return clientes.stream().filter(c -> c.id().equals(id)).findFirst();
    }
}
