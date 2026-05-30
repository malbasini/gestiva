package com.gestiva.accounting.v2.report.web;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class AccountLedgerXlsxExportService {

    public byte[] export(AccountLedgerView ledger) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Mastrino conto");

            CellStyle headerStyle = createHeaderStyle(workbook);

            String[] headers = {
                    "Conto",
                    "Dal",
                    "Al",
                    "Data",
                    "Numero",
                    "Causale",
                    "Descrizione",
                    "Dare",
                    "Avere",
                    "Saldo progressivo"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            if (ledger.getRows() != null) {
                String accountLabel = buildAccountLabel(ledger);

                for (AccountLedgerRowView rowView : ledger.getRows()) {
                    Row row = sheet.createRow(rowIdx++);

                    row.createCell(0).setCellValue(nvl(accountLabel));
                    row.createCell(1).setCellValue(nvl(ledger.getFormattedDateFrom()));
                    row.createCell(2).setCellValue(nvl(ledger.getFormattedDateTo()));
                    row.createCell(3).setCellValue(nvl(rowView.getFormattedEntryDate()));
                    row.createCell(4).setCellValue(nvl(rowView.getEntryNumber()));
                    row.createCell(5).setCellValue(nvl(rowView.getCausalCodeLabel()));
                    row.createCell(6).setCellValue(nvl(rowView.getDescription()));
                    row.createCell(7).setCellValue(nvl(rowView.getFormattedDebit()));
                    row.createCell(8).setCellValue(nvl(rowView.getFormattedCredit()));
                    row.createCell(9).setCellValue(nvl(rowView.getFormattedProgressiveBalance()));
                }
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException ex) {
            throw new IllegalStateException("Errore nella generazione del file XLSX del mastrino conto.", ex);
        }
    }

    private String buildAccountLabel(AccountLedgerView ledger) {
        String code = ledger.getAccountCode() == null ? "" : ledger.getAccountCode();
        String name = ledger.getAccountName() == null ? "" : ledger.getAccountName();
        return code.isBlank() ? name : code + " - " + name;
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
