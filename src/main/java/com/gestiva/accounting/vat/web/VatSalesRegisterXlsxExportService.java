package com.gestiva.accounting.vat.web;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class VatSalesRegisterXlsxExportService {

    public byte[] export(List<VatSalesRegisterRowView> rows) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Registro IVA vendite");

            CellStyle headerStyle = createHeaderStyle(workbook);

            String[] headers = {
                    "Data",
                    "Numero fattura",
                    "Cliente",
                    "Aliquota",
                    "Imponibile",
                    "IVA",
                    "Totale"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (VatSalesRegisterRowView row : rows) {
                Row excelRow = sheet.createRow(rowIdx++);

                excelRow.createCell(0).setCellValue(nvl(row.getFormattedInvoiceDate()));
                excelRow.createCell(1).setCellValue(nvl(row.getInvoiceNumber()));
                excelRow.createCell(2).setCellValue(nvl(row.getCustomerName()));
                excelRow.createCell(3).setCellValue(nvl(row.getFormattedTaxPct()));
                excelRow.createCell(4).setCellValue(nvl(row.getFormattedTaxableAmount()));
                excelRow.createCell(5).setCellValue(nvl(row.getFormattedTaxAmount()));
                excelRow.createCell(6).setCellValue(nvl(row.getFormattedTotalAmount()));
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException ex) {
            throw new IllegalStateException("Errore nella generazione del file XLSX del registro IVA vendite.", ex);
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