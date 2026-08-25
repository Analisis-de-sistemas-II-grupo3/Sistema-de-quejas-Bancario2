package com.banco.quejas;

import java.util.List;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Verificación ejecutable de las reglas principales del CU01 V1.3. */
public class PruebasCU01 {
    public static void main(String[] args) {
        PasswordEncoder codificador = new BCryptPasswordEncoder();
        RepositorioClientes clientes = new RepositorioClientes(List.of(
                new Cliente("CLI-1", "Ana López", "1", "ana@correo.com", "555", List.of(new CuentaBancaria("1", true)))));
        RepositorioUsuarios usuarios = new RepositorioUsuarios(List.of(
                new Usuario("U1", "ana.lopez", codificador.encode("Clave#123"), "Cliente", "Ana López", "CLI-1"),
                new Usuario("U2", "carlos", codificador.encode("Clave#456"), "Agente de Atención", "Carlos", null),
                new Usuario("U3", "inactivo", codificador.encode("Clave#789"), "Auditor", "Inactivo", null, false)));
        BitacoraAutenticacion bitacora = new BitacoraAutenticacion();
        ServicioAutenticacion servicio = new ServicioAutenticacion(usuarios, clientes, codificador, bitacora);

        ContextoAutenticacion cliente = servicio.autenticar("ana.lopez", "Clave#123", "127.0.0.1");
        verificar("Cliente", cliente.sesion().rol());
        verificar("CLI-1", cliente.cliente().id());
        verificar("/casos/nuevo", RutaBandeja.paraRol(cliente.sesion().rol()));

        ContextoAutenticacion agente = servicio.autenticar("carlos", "Clave#456", "127.0.0.1");
        verificar(null, agente.cliente());
        verificar("/bandeja/agente", RutaBandeja.paraRol(agente.sesion().rol()));

        esperarError(() -> servicio.autenticar("ana.lopez", "incorrecta", "127.0.0.1"));
        esperarError(() -> servicio.autenticar("no.existe", "cualquiera", "127.0.0.1"));
        esperarError(() -> servicio.autenticar("inactivo", "Clave#789", "127.0.0.1"));
        esperarError(() -> servicio.autenticar("", "", "127.0.0.1"));

        verificar("/admin", RutaBandeja.paraRol("Administrador"));
        verificar("/auditoria", RutaBandeja.paraRol("Auditor"));
        verificar("/bandeja/supervisor", RutaBandeja.paraRol("Supervisor"));
        verificar("/login", RutaBandeja.paraRol("Rol Inexistente"));

        verificar(6, bitacora.consultar().size());
        System.out.println("Pruebas CU01 V1.3 aprobadas.");
    }

    private static void verificar(Object esperado, Object real) {
        boolean iguales = esperado == null ? real == null : esperado.equals(real);
        if (!iguales) throw new AssertionError("Esperado: " + esperado + "; obtenido: " + real);
    }
    private static void esperarError(Runnable accion) {
        try { accion.run(); throw new AssertionError("Se esperaba un error de autenticación."); }
        catch (AutenticacionException esperado) { }
    }
}
