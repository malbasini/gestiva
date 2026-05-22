package com.gestiva.inventory.valuation.web;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class InventoryConsistencyCheckXlsxExportService {

    public byte[] export(List<InventoryConsistencyCheckItemView> rows) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Controllo coerenza");

            CellStyle headerStyle = createHeaderStyle(workbook);

            String[] headers = {
                    "Articolo",
                    "Giacenza ledger",
                    "Giacenza valorizzata",
                    "Differenza",
                    "Stato"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (InventoryConsistencyCheckItemView row : rows) {
                Row excelRow = sheet.createRow(rowIdx++);

                excelRow.createCell(0).setCellValue(buildItemLabel(row));
                excelRow.createCell(1).setCellValue(nvl(row.getFormattedLedgerQty()));
                excelRow.createCell(2).setCellValue(nvl(row.getFormattedLayerQty()));
                excelRow.createCell(3).setCellValue(nvl(row.getFormattedDifferenceQty()));
                excelRow.createCell(4).setCellValue(nvl(row.getStatusLabel()));
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException ex) {
            throw new IllegalStateException("Errore nella generazione del file XLSX del controllo coerenza.", ex);
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        return style;
    }

    private String buildItemLabel(InventoryConsistencyCheckItemView row) {
        String code = row.getItemCode() == null ? "" : row.getItemCode();
        String name = row.getItemName() == null ? "" : row.getItemName();
        return code + " - " + name;
    }

    private String nvl(String value) {
        return value == null ? "" : value;
    }
}