package com.umg.quejasbancario.service;

import com.umg.quejasbancario.config.AppProperties;
import com.umg.quejasbancario.entity.Caso;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * CU-11 Enviar Notificacion. Implementa las 6 plantillas de RN09.
 * Envia el correo por SMTP real y, tanto en exito como en falla (FA01),
 * deja constancia en la Bitacora de Envio de Correos (RN15).
 *
 * El envio nunca bloquea ni revierte el flujo de negocio que lo origino
 * (RN09/CU-11 FA01: "El sistema continua con el flujo del caso de uso de
 * origen, sin bloquear el proceso principal").
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificacionService {

    private final JavaMailSender mailSender;
    private final AppProperties appProperties;
    private final BitacoraRegistroService bitacoraRegistroService;

    private static final String FOOTER = "\n\n*** Esta es una correspondencia autogenerada por el Sistema de Quejas Bancario. Por favor NO RESPONDA a este correo.";

    /** #1 Aviso al Agente de nuevo caso asignado. */
    public void notificarNuevoCasoAsignado(Caso caso, String correoAgente) {
        String asunto = "Nuevo caso asignado: " + caso.getNumeroCaso();
        String cuerpo = String.format(
                "Se le informa que le fue asignado un nuevo caso de forma automática. Tipo de Caso: '%s', Número de Caso: '%s'. Se envía este aviso para su seguimiento desde su bandeja.",
                caso.getTipoCaso().getNombre(), caso.getNumeroCaso());
        enviarYRegistrar(caso, correoAgente, "Aviso #1 - Nuevo caso asignado", asunto, cuerpo);
    }

    /** #2 Aviso al Cliente de caso registrado. */
    public void notificarCasoRegistrado(Caso caso, String correoCliente) {
        String asunto = "Su caso fue registrado: " + caso.getNumeroCaso();
        String cuerpo = String.format(
                "Se le informa que su caso fue registrado con éxito. Número de Caso: '%s'. Podrá dar seguimiento al estado de su caso desde la plataforma.",
                caso.getNumeroCaso());
        enviarYRegistrar(caso, correoCliente, "Aviso #2 - Caso registrado", asunto, cuerpo);
    }

    /** #3 Aviso al Cliente de cambio de estado. */
    public void notificarCambioEstado(Caso caso, String correoCliente, String estadoAnterior, String estadoNuevo) {
        String asunto = "Actualización de su caso: " + caso.getNumeroCaso();
        String cuerpo = String.format(
                "Se le informa que su caso '%s' cambió de estado: '%s' a '%s'.",
                caso.getNumeroCaso(), estadoAnterior, estadoNuevo);
        enviarYRegistrar(caso, correoCliente, "Aviso #3 - Cambio de estado", asunto, cuerpo);
    }

    /** #4 Aviso al Cliente de caso resuelto. */
    public void notificarCasoResuelto(Caso caso, String correoCliente) {
        String asunto = "Su caso fue resuelto: " + caso.getNumeroCaso();
        String cuerpo = String.format(
                "Se le informa que su caso '%s' fue resuelto. Puede consultar el detalle de la resolución desde la plataforma.",
                caso.getNumeroCaso());
        enviarYRegistrar(caso, correoCliente, "Aviso #4 - Caso resuelto", asunto, cuerpo);
    }

    /** #5 Aviso al Supervisor de solicitud de reasignacion. */
    public void notificarSolicitudReasignacion(Caso caso, String correoSupervisor, String nombreAgente, String motivo) {
        String asunto = "Solicitud de reasignación: " + caso.getNumeroCaso();
        String cuerpo = String.format(
                "Se le informa que el Agente '%s' solicitó la reasignación del caso '%s'. Motivo: '%s'.",
                nombreAgente, caso.getNumeroCaso(), motivo);
        enviarYRegistrar(caso, correoSupervisor, "Aviso #5 - Solicitud de reasignación", asunto, cuerpo);
    }

    /** #6 Aviso al Agente de reasignacion aprobada. */
    public void notificarReasignacionAprobada(Caso caso, String correoAgente, String nombreAgente) {
        String asunto = "Reasignación aprobada: " + caso.getNumeroCaso();
        String cuerpo = String.format(
                "se le informa al Agente \"%s\" que su solicitud de reasignación fue aprobada.",
                nombreAgente);
        enviarYRegistrar(caso, correoAgente, "Aviso #6 - Reasignación aprobada", asunto, cuerpo);
    }

    /** Notificacion adicional (fuera de RN09 numerada) para cierre de caso, referida en CU-09/RF32. */
    public void notificarCasoCerrado(Caso caso, String correoCliente) {
        String asunto = "Su caso fue cerrado: " + caso.getNumeroCaso();
        String cuerpo = String.format(
                "Se le informa que su caso '%s' fue cerrado formalmente. Gracias por utilizar el Sistema de Quejas Bancario.",
                caso.getNumeroCaso());
        enviarYRegistrar(caso, correoCliente, "Aviso - Caso cerrado", asunto, cuerpo);
    }

    /** Envio de credenciales / enlace de recuperacion (CU-01) - no aplica a un caso especifico. */
    public void enviarEnlaceRecuperacion(String correoDestino, String enlace) {
        String asunto = "Recuperación de contraseña - Sistema de Quejas Bancario";
        String cuerpo = "Recibimos una solicitud para restablecer su contraseña. Utilice el siguiente enlace (vigencia limitada):\n\n"
                + enlace;
        enviarYRegistrar(null, correoDestino, "Recuperación de contraseña", asunto, cuerpo);
    }

    private void enviarYRegistrar(Caso caso, String destinatario, String tipoNotificacion, String asunto, String cuerpo) {
        String descripcion;
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setTo(destinatario);
            mensaje.setFrom(appProperties.getMail().getFrom());
            mensaje.setSubject(asunto);
            mensaje.setText(cuerpo + FOOTER);
            mailSender.send(mensaje);
            descripcion = "Se envió notificación '" + tipoNotificacion + "' a " + destinatario + ".";
        } catch (Exception ex) {
            // FA01: falla en el envio - se registra el error, sin bloquear el flujo de origen.
            log.warn("Fallo el envio de correo a {} ({}): {}", destinatario, tipoNotificacion, ex.getMessage());
            descripcion = "Fallo el envío de la notificación '" + tipoNotificacion + "' a " + destinatario + ". Error: " + ex.getMessage();
        }
        bitacoraRegistroService.registrarEventoCorreo(caso, destinatario, tipoNotificacion, descripcion);
    }
}
