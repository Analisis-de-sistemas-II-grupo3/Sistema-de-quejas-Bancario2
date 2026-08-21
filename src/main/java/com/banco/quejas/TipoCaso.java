package com.banco.quejas;

public enum TipoCaso {
    QUEJA("Q"), RECLAMO("R"), DENUNCIA("D"), SUGERENCIA("S");

    private final String prefijo;
    TipoCaso(String prefijo) { this.prefijo = prefijo; }
    public String prefijo() { return prefijo; }
}
