/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.banco.quejas;

/**
 *
 * @author José Chic
 */
import java.time.LocalDateTime;

/** Fila de la tabla "Mis Casos" (CU04 V1.1, paso 2): folio, tipo, fechas y estado actual. */
public record ResumenCasoCliente(String folio, TipoCaso tipo, LocalDateTime fechaRegistro,
                                 EstadoCaso estado, LocalDateTime fechaUltimaActualizacion) { }
