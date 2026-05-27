package com.gestiva.accounting.v2.report.web;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class TrialBalanceXlsxExportService {

    public byte[] export(TrialBalanceView trialBalance) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Bilancino di verifica");

            CellStyle headerStyle = createHeaderStyle(workbook);

            String[] headers = {
                    "Dal",
                    "Al",
                    "Codice",
                    "Conto",
                    "Tipo",
                    "Natura",
                    "Dare",
                    "Avere",
                    "Saldo"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            if (trialBalance.getRows() != null) {
                for (TrialBalanceRowView row : trialBalance.getRows()) {
                    Row excelRow = sheet.createRow(rowIdx++);

                    excelRow.createCell(0).setCellValue(nvl(trialBalance.getFormattedDateFrom()));
                    excelRow.createCell(1).setCellValue(nvl(trialBalance.getFormattedDateTo()));
                    excelRow.createCell(2).setCellValue(nvl(row.getAccountCode()));
                    excelRow.createCell(3).setCellValue(nvl(row.getAccountName()));
                    excelRow.createCell(4).setCellValue(nvl(row.getAccountType()));
                    excelRow.createCell(5).setCellValue(nvl(row.getNature()));
                    excelRow.createCell(6).setCellValue(nvl(row.getFormattedTotalDebit()));
                    excelRow.createCell(7).setCellValue(nvl(row.getFormattedTotalCredit()));
                    excelRow.createCell(8).setCellValue(nvl(row.getFormattedBalance()));
                }
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException ex) {
            throw new IllegalStateException("Errore nella generazione del file XLSX del bilancino di verifica.", ex);
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