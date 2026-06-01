package com.gestiva.accounting.v2.report.web;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class IncomeStatementXlsxExportService {

    public byte[] export(IncomeStatementView view) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Conto economico");
            CellStyle headerStyle = createHeaderStyle(workbook);

            String[] headers = {
                    "Dal",
                    "Al",
                    "Sezione",
                    "Codice",
                    "Conto",
                    "Importo"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            if (view.getSections() != null) {
                for (IncomeStatementSectionView section : view.getSections()) {
                    if (section.getRows() == null) {
                        continue;
                    }

                    for (IncomeStatementRowView rowView : section.getRows()) {
                        Row row = sheet.createRow(rowIdx++);

                        row.createCell(0).setCellValue(nvl(view.getFormattedDateFrom()));
                        row.createCell(1).setCellValue(nvl(view.getFormattedDateTo()));
                        row.createCell(2).setCellValue(nvl(section.getSectionLabel()));
                        row.createCell(3).setCellValue(nvl(rowView.getAccountCode()));
                        row.createCell(4).setCellValue(nvl(rowView.getAccountName()));
                        row.createCell(5).setCellValue(nvl(rowView.getFormattedAmount()));
                    }
                }
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException ex) {
            throw new IllegalStateException("Errore nella generazione del file XLSX del conto economico.", ex);
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