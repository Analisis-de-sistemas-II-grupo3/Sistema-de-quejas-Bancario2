package com.banco.quejas;

/** Identidad de la sesión que CU01 proporcionará al resto de casos de uso. */
public record ContextoAutenticacion(Cliente cliente, SesionUsuario sesion) { }
