package com.banco.quejas;

import java.time.LocalDate;
import java.util.List;

/** Verificación ejecutable de las reglas principales del CU02 V1.4. */
public class PruebasCU02 {
    public static void main(String[] args) {
        Cliente cliente = new Cliente("CLI-1", "Ana López", "1234567890101", "ana@correo.com", "55550101", List.of(new CuentaBancaria("123", true)));
        SesionUsuario sesion = new SesionUsuario("ana", "Cliente", "127.0.0.1");
        RegistradorCasos servicio = new RegistradorCasos(List.of(new ProductoServicio("CTA", "Cuenta de ahorro")), List.of(new AgenteAtencion("AG-1", "Carlos", true, 0, 5)));

        Caso caso = servicio.registrar(cliente, sesion, solicitud(TipoCaso.QUEJA, "CTA", "Descripción válida"));
        verificar("CLI-1", caso.clienteId());
        verificar(EstadoCaso.ASIGNADO, caso.estado());
        verificar("Q-00001-" + java.time.Year.now().getValue(), caso.folio());
        verificar(2, servicio.consultarBitacora().size());
        verificar(2, servicio.consultarNotificaciones().size());

        RegistradorCasos sinAgentes = new RegistradorCasos(List.of(new ProductoServicio("CTA", "Cuenta de ahorro")), List.of());
        verificar(EstadoCaso.RECIBIDO, sinAgentes.registrar(cliente, sesion, solicitud(TipoCaso.DENUNCIA, "CTA", "Denuncia válida")).estado());
        Cliente sinCuentaActiva = new Cliente("CLI-2", "Luis", "1", "luis@correo.com", "", List.of(new CuentaBancaria("0", false)));
        esperarError(() -> servicio.registrar(sinCuentaActiva, sesion, solicitud(TipoCaso.RECLAMO, "CTA", "Detalle")));
        esperarError(() -> servicio.registrar(cliente, new SesionUsuario("ana", "Agente de Atención", "127.0.0.1"), solicitud(TipoCaso.SUGERENCIA, "CTA", "Detalle")));
        esperarError(() -> servicio.registrar(null, null, solicitud(TipoCaso.DENUNCIA, "CTA", "Detalle")));
        esperarError(() -> servicio.registrar(cliente, sesion, solicitud(TipoCaso.QUEJA, "FUERA", "Detalle")));
        esperarError(() -> servicio.registrar(cliente, sesion, solicitud(TipoCaso.QUEJA, "CTA", "x".repeat(2001))));
        System.out.println("Pruebas CU02 V1.4 aprobadas.");
    }
    private static SolicitudRegistroCaso solicitud(TipoCaso tipo, String producto, String descripcion) {
        return new SolicitudRegistroCaso(tipo, producto, "Banca en línea", LocalDate.now(), descripcion, null);
    }
    private static void verificar(Object esperado, Object real) { if (!esperado.equals(real)) throw new AssertionError("Esperado: " + esperado + "; obtenido: " + real); }
    private static void esperarError(Runnable accion) { try { accion.run(); throw new AssertionError("Se esperaba una validación rechazada."); } catch (RegistroCasoException esperado) { } }
}
