package com.banco.quejas;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Lógica del CU01 V1.3 - Iniciar Sesión. RN01: solo usuarios con rol válido y contraseña
 * correcta pueden acceder. FA01: credenciales inválidas produce un mensaje genérico
 * (no distingue si el usuario no existe o la contraseña es incorrecta).
 */
@Service
public class ServicioAutenticacion {
    private static final String MENSAJE_CREDENCIALES_INVALIDAS = "Usuario o contraseña incorrectos.";

    private final RepositorioUsuarios repositorioUsuarios;
    private final RepositorioClientes repositorioClientes;
    private final PasswordEncoder codificador;
    private final BitacoraAutenticacion bitacora;

    public ServicioAutenticacion(RepositorioUsuarios repositorioUsuarios, RepositorioClientes repositorioClientes,
                                 PasswordEncoder codificador, BitacoraAutenticacion bitacora) {
        this.repositorioUsuarios = repositorioUsuarios;
        this.repositorioClientes = repositorioClientes;
        this.codificador = codificador;
        this.bitacora = bitacora;
    }

    /** Flujo básico pasos 3-7. Lanza AutenticacionException en el caso FA01. */
    public ContextoAutenticacion autenticar(String usuario, String contrasena, String ip) {
        if (usuario == null || usuario.isBlank() || contrasena == null || contrasena.isBlank()) {
            registrarFallo(usuario, ip, "Usuario o contraseña vacíos.");
            throw new AutenticacionException("Debe ingresar usuario y contraseña.");
        }
        Usuario encontrado = repositorioUsuarios.buscarPorUsuario(usuario).orElse(null);
        if (encontrado == null || !encontrado.activo() || !codificador.matches(contrasena, encontrado.contrasenaHash())) {
            registrarFallo(usuario, ip, "Credenciales inválidas o usuario inactivo.");
            throw new AutenticacionException(MENSAJE_CREDENCIALES_INVALIDAS);
        }
        Cliente cliente = encontrado.clienteId() == null ? null : repositorioClientes.buscarPorId(encontrado.clienteId()).orElse(null);
        SesionUsuario sesion = new SesionUsuario(encontrado.usuario(), encontrado.rol(), ip);
        bitacora.registrar("INICIO DE SESIÓN", sesion.usuario(), sesion.rol(), ip,
                "Inicio de sesión exitoso para el rol " + sesion.rol() + ".");
        return new ContextoAutenticacion(cliente, sesion);
    }

    private void registrarFallo(String usuarioIntentado, String ip, String detalle) {
        bitacora.registrar("INTENTO FALLIDO", usuarioIntentado == null || usuarioIntentado.isBlank() ? "(vacío)" : usuarioIntentado,
                "N/D", ip, detalle);
    }
}
