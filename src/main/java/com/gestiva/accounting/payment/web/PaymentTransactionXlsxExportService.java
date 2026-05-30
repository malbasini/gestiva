package com.gestiva.accounting.payment.web;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class PaymentTransactionXlsxExportService {

    public byte[] export(Page<PaymentTransactionListItemView> page) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Incassi e pagamenti");

            CellStyle headerStyle = createHeaderStyle(workbook);

            String[] headers = {
                    "Data",
                    "Tipo",
                    "Soggetto",
                    "Documento",
                    "Importo",
                    "Metodo",
                    "Riferimento",
                    "Stato scadenza"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            if (page != null && page.getContent() != null) {
                for (PaymentTransactionListItemView rowView : page.getContent()) {
                    Row row = sheet.createRow(rowIdx++);

                    row.createCell(0).setCellValue(nvl(rowView.getFormattedPaymentDate()));
                    row.createCell(1).setCellValue(nvl(rowView.getDirectionLabel()));
                    row.createCell(2).setCellValue(nvl(rowView.getPartyLabel()));
                    row.createCell(3).setCellValue(nvl(rowView.getDocumentLabel()));
                    row.createCell(4).setCellValue(nvl(rowView.getFormattedAmount()));
                    row.createCell(5).setCellValue(nvl(rowView.getPaymentMethod()));
                    row.createCell(6).setCellValue(nvl(rowView.getReference()));
                    row.createCell(7).setCellValue(resolveDueStatusLabel(rowView.getDueStatus()));
                }
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException ex) {
            throw new IllegalStateException("Errore nella generazione del file XLSX degli incassi/pagamenti.", ex);
        }
    }

    private String resolveDueStatusLabel(String status) {
        if (status == null) return "";
        return switch (status.toUpperCase()) {
            case "PAID" -> "SALDATA";
            case "PARTIALLY_PAID" -> "PARZIALMENTE SALDATA";
            case "CANCELLED" -> "ANNULLATA";
            case "OPEN" -> "APERTA";
            default -> status;
        };
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        return style;
    }

    private String nvl(String value) {
        return value == null ? "" : value;
    }
}
