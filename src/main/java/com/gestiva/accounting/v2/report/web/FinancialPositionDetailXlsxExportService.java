package com.gestiva.accounting.v2.report.web;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class FinancialPositionDetailXlsxExportService {

    public byte[] export(FinancialPositionDetailView view) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Situazione contabile V2");

            CellStyle headerStyle = createHeaderStyle(workbook);

            String[] headers = {
                    "Dal",
                    "Al",
                    "Classe",
                    "Codice",
                    "Conto",
                    "Saldo"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            if (view.getSections() != null) {
                for (FinancialPositionSectionView section : view.getSections()) {
                    if (section.getDetails() == null) {
                        continue;
                    }

                    for (FinancialPositionDetailRowView detail : section.getDetails()) {
                        Row row = sheet.createRow(rowIdx++);

                        row.createCell(0).setCellValue(nvl(view.getFormattedDateFrom()));
                        row.createCell(1).setCellValue(nvl(view.getFormattedDateTo()));
                        row.createCell(2).setCellValue(nvl(section.getAccountTypeLabel()));
                        row.createCell(3).setCellValue(nvl(detail.getAccountCode()));
                        row.createCell(4).setCellValue(nvl(detail.getAccountName()));
                        row.createCell(5).setCellValue(nvl(detail.getFormattedAmount()));
                    }
                }
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException ex) {
            throw new IllegalStateException("Errore nella generazione del file XLSX della situazione contabile per classi.", ex);
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
