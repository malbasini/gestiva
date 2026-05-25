package com.gestiva.accounting.vat.web;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class VatSettlementXlsxExportService {

    public byte[] export(VatSettlementView settlement) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Liquidazione IVA");

            CellStyle headerStyle = createHeaderStyle(workbook);

            String[] headers = {
                    "Dal",
                    "Al",
                    "Imponibile vendite",
                    "IVA vendite",
                    "Imponibile acquisti",
                    "IVA acquisti",
                    "Saldo IVA",
                    "Esito"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            Row row = sheet.createRow(1);
            row.createCell(0).setCellValue(nvl(settlement.getFormattedDateFrom()));
            row.createCell(1).setCellValue(nvl(settlement.getFormattedDateTo()));
            row.createCell(2).setCellValue(nvl(settlement.getFormattedSalesTaxableAmount()));
            row.createCell(3).setCellValue(nvl(settlement.getFormattedSalesTaxAmount()));
            row.createCell(4).setCellValue(nvl(settlement.getFormattedPurchaseTaxableAmount()));
            row.createCell(5).setCellValue(nvl(settlement.getFormattedPurchaseTaxAmount()));
            row.createCell(6).setCellValue(nvl(settlement.getFormattedVatBalance()));
            row.createCell(7).setCellValue(nvl(settlement.getBalanceTypeLabel()));

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException ex) {
            throw new IllegalStateException("Errore nella generazione del file XLSX della liquidazione IVA.", ex);
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