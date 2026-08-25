package com.banco.quejas;

/** Datos que recibe la pantalla de CU01 (flujo básico, pasos 2-3). */
public class CredencialesLogin {
    private String usuario = "";
    private String contrasena = "";
    public String getUsuario() { return usuario; } public void setUsuario(String v) { usuario = v; }
    public String getContrasena() { return contrasena; } public void setContrasena(String v) { contrasena = v; }
}
