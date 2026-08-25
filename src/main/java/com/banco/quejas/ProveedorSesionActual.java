package com.banco.quejas;

/** Puerto de integración con CU01. La implementación actual es solo para desarrollo local. */
public interface ProveedorSesionActual {
    ContextoAutenticacion obtener(String ipCliente);
}
