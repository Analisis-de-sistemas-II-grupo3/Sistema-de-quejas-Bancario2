package com.banco.quejas;

/**
 * Cuenta de acceso al sistema (CU01). Un Usuario con rol "Cliente" está vinculado
 * a un {@link Cliente} por clienteId; los demás roles no tienen cliente asociado.
 * La base de datos sustituirá este repositorio en memoria (ver README).
 */
public record Usuario(String id, String usuario, String contrasenaHash, String rol,
                      String nombreCompleto, String clienteId, boolean activo) {
    public Usuario(String id, String usuario, String contrasenaHash, String rol, String nombreCompleto, String clienteId) {
        this(id, usuario, contrasenaHash, rol, nombreCompleto, clienteId, true);
    }
}
