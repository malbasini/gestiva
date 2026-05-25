package com.gestiva.accounting.v2.report.web;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class FinancialPositionSummaryXlsxExportService {

    public byte[] export(FinancialPositionSummaryView summary) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Situazione contabile");

            CellStyle headerStyle = createHeaderStyle(workbook);

            String[] headers = {
                    "Dal",
                    "Al",
                    "Classe",
                    "Saldo"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            if (summary.getRows() != null) {
                for (FinancialPositionSummaryRowView row : summary.getRows()) {
                    Row excelRow = sheet.createRow(rowIdx++);

                    excelRow.createCell(0).setCellValue(nvl(summary.getFormattedDateFrom()));
                    excelRow.createCell(1).setCellValue(nvl(summary.getFormattedDateTo()));
                    excelRow.createCell(2).setCellValue(nvl(row.getAccountTypeLabel()));
                    excelRow.createCell(3).setCellValue(nvl(row.getFormattedAmount()));
                }
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException ex) {
            throw new IllegalStateException("Errore nella generazione del file XLSX della situazione contabile.", ex);
        }
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