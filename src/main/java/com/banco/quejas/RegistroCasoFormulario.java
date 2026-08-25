package com.banco.quejas;

import java.time.LocalDate;

/** Datos que recibe la pantalla del CU02 V1.4; no incluye datos anónimos. */
public class RegistroCasoFormulario {
    private TipoCaso tipo;
    private String productoId = "";
    private String sucursalCanal = "";
    private LocalDate fechaHecho;
    private String descripcion = "";
    public TipoCaso getTipo() { return tipo; } public void setTipo(TipoCaso v) { tipo = v; }
    public String getProductoId() { return productoId; } public void setProductoId(String v) { productoId = v; }
    public String getSucursalCanal() { return sucursalCanal; } public void setSucursalCanal(String v) { sucursalCanal = v; }
    public LocalDate getFechaHecho() { return fechaHecho; } public void setFechaHecho(LocalDate v) { fechaHecho = v; }
    public String getDescripcion() { return descripcion; } public void setDescripcion(String v) { descripcion = v; }
}
