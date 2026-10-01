package com.umg.quejasbancario.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import com.umg.quejasbancario.dto.response.BitacoraCasoResponse;
import com.umg.quejasbancario.entity.Caso;
import com.umg.quejasbancario.entity.enums.EstadoCaso;
import com.umg.quejasbancario.repository.CasoRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * CU-12 Generar Reporte de Casos (Administrador/Supervisor) y CU-13 Generar
 * Reporte de Auditoria (Auditor). Formatos soportados: Excel (.xlsx) y PDF,
 * conforme a RF39.
 */
@Service
@RequiredArgsConstructor
public class ReporteService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final CasoRepository casoRepository;

    /** Obtiene los casos que entraran al reporte, aplicando los mismos filtros de CU-10. */
    public List<Caso> obtenerCasosParaReporte(String numeroCaso, Integer idTipoCaso, String estado,
                                               LocalDateTime desde, LocalDateTime hasta) {
        EstadoCaso estadoEnum = (estado != null && !estado.isBlank()) ? EstadoCaso.fromValor(estado) : null;
        Specification<Caso> spec = Specification.where(CasoSpecifications.numeroCaso(numeroCaso))
                .and(CasoSpecifications.tipoCaso(idTipoCaso))
                .and(CasoSpecifications.estado(estadoEnum))
                .and(CasoSpecifications.fechaDesde(desde))
                .and(CasoSpecifications.fechaHasta(hasta));
        return casoRepository.findAll(spec);
    }

    // ============================ CU-12: Reporte de Casos ============================

    public byte[] reporteCasosExcel(List<Caso> casos) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Reporte de Casos");
            CellStyle headerStyle = estiloEncabezado(workbook);

            String[] columnas = {"No. Caso", "Fecha Registro", "Tipo", "Cliente", "Agente Asignado", "Estado", "Fecha Cierre"};
            Row header = sheet.createRow(0);
            for (int i = 0; i < columnas.length; i++) {
                org.apache.poi.ss.usermodel.Cell c = header.createCell(i);
                c.setCellValue(columnas[i]);
                c.setCellStyle(headerStyle);
            }

            int fila = 1;
            for (Caso caso : casos) {
                Row row = sheet.createRow(fila++);
                row.createCell(0).setCellValue(caso.getNumeroCaso());
                row.createCell(1).setCellValue(caso.getFechaRegistro() != null ? caso.getFechaRegistro().format(FMT) : "");
                row.createCell(2).setCellValue(caso.getTipoCaso().getNombre());
                row.createCell(3).setCellValue(caso.getNombreClienteCaso());
                row.createCell(4).setCellValue(caso.getAgenteAsignado() != null ? caso.getAgenteAsignado().getNombreCompleto() : "Sin asignar");
                row.createCell(5).setCellValue(caso.getEstado().getValor());
                row.createCell(6).setCellValue(caso.getFechaCierre() != null ? caso.getFechaCierre().format(FMT) : "");
            }

            for (int i = 0; i < columnas.length; i++) sheet.autoSizeColumn(i);

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("No fue posible generar el reporte en Excel.", e);
        }
    }

    public byte[] reporteCasosPdf(List<Caso> casos) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             PdfDocument pdfDoc = new PdfDocument(new PdfWriter(out));
             Document document = new Document(pdfDoc)) {

            document.add(new Paragraph("Sistema de Quejas Bancario").setBold().setFontSize(16));
            document.add(new Paragraph("Reporte de Casos").setFontSize(12));
            document.add(new Paragraph(" "));

            Table table = new Table(UnitValue.createPercentArray(new float[]{15, 13, 12, 20, 18, 12, 13}))
                    .useAllAvailableWidth();
            agregarEncabezado(table, "No. Caso", "Fecha Registro", "Tipo", "Cliente", "Agente", "Estado", "Fecha Cierre");

            for (Caso caso : casos) {
                table.addCell(celda(caso.getNumeroCaso()));
                table.addCell(celda(caso.getFechaRegistro() != null ? caso.getFechaRegistro().format(FMT) : ""));
                table.addCell(celda(caso.getTipoCaso().getNombre()));
                table.addCell(celda(caso.getNombreClienteCaso()));
                table.addCell(celda(caso.getAgenteAsignado() != null ? caso.getAgenteAsignado().getNombreCompleto() : "Sin asignar"));
                table.addCell(celda(caso.getEstado().getValor()));
                table.addCell(celda(caso.getFechaCierre() != null ? caso.getFechaCierre().format(FMT) : ""));
            }
            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("No fue posible generar el reporte en PDF.", e);
        }
    }

    // ============================ CU-13: Reporte de Auditoria ============================

    public byte[] reporteAuditoriaExcel(List<BitacoraCasoResponse> eventos) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Reporte de Auditoria");
            CellStyle headerStyle = estiloEncabezado(workbook);

            String[] columnas = {"Fecha/Hora", "No. Caso", "Usuario", "Rol", "IP", "Estado Anterior", "Estado Nuevo", "Descripción"};
            Row header = sheet.createRow(0);
            for (int i = 0; i < columnas.length; i++) {
                org.apache.poi.ss.usermodel.Cell c = header.createCell(i);
                c.setCellValue(columnas[i]);
                c.setCellStyle(headerStyle);
            }

            int fila = 1;
            for (BitacoraCasoResponse ev : eventos) {
                Row row = sheet.createRow(fila++);
                row.createCell(0).setCellValue(ev.getFechaHora() != null ? ev.getFechaHora().format(FMT) : "");
                row.createCell(1).setCellValue(ev.getNumeroCaso());
                row.createCell(2).setCellValue(ev.getUsuario());
                row.createCell(3).setCellValue(ev.getRolEjecuta());
                row.createCell(4).setCellValue(ev.getIp());
                row.createCell(5).setCellValue(ev.getEstadoAnterior());
                row.createCell(6).setCellValue(ev.getEstadoNuevo());
                row.createCell(7).setCellValue(ev.getDescripcionEvento());
            }
            for (int i = 0; i < columnas.length; i++) sheet.autoSizeColumn(i);

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("No fue posible generar el reporte en Excel.", e);
        }
    }

    public byte[] reporteAuditoriaPdf(List<BitacoraCasoResponse> eventos) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             PdfDocument pdfDoc = new PdfDocument(new PdfWriter(out));
             Document document = new Document(pdfDoc)) {

            document.add(new Paragraph("Sistema de Quejas Bancario").setBold().setFontSize(16));
            document.add(new Paragraph("Reporte de Auditoría").setFontSize(12));
            document.add(new Paragraph(" "));

            Table table = new Table(UnitValue.createPercentArray(new float[]{12, 10, 14, 10, 10, 11, 11, 22}))
                    .useAllAvailableWidth();
            agregarEncabezado(table, "Fecha/Hora", "No. Caso", "Usuario", "Rol", "IP", "Est. Ant.", "Est. Nuevo", "Descripción");

            for (BitacoraCasoResponse ev : eventos) {
                table.addCell(celda(ev.getFechaHora() != null ? ev.getFechaHora().format(FMT) : ""));
                table.addCell(celda(ev.getNumeroCaso()));
                table.addCell(celda(ev.getUsuario()));
                table.addCell(celda(ev.getRolEjecuta()));
                table.addCell(celda(ev.getIp()));
                table.addCell(celda(ev.getEstadoAnterior()));
                table.addCell(celda(ev.getEstadoNuevo()));
                table.addCell(celda(ev.getDescripcionEvento()));
            }
            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("No fue posible generar el reporte en PDF.", e);
        }
    }

    // ============================ Utilidades ============================

    private CellStyle estiloEncabezado(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private void agregarEncabezado(Table table, String... columnas) {
        for (String col : columnas) {
            table.addHeaderCell(new Cell().add(new Paragraph(col).setBold()));
        }
    }

    private Cell celda(String texto) {
        return new Cell().add(new Paragraph(texto != null ? texto : ""));
    }
}
