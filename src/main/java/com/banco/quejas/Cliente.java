package com.banco.quejas;

import java.util.List;

public record Cliente(String id, String nombreCompleto, String dpiNit, String correo,
                      String telefono, List<CuentaBancaria> cuentas) {
    public boolean tieneCuentaActiva(String numero) {
        return cuentas.stream().anyMatch(c -> c.numero().equals(numero) && c.activa());
    }
}
