package com.gestiva.inventory.valuation.web;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class OutboundValuationXlsxExportService {

    public byte[] export(List<OutboundValuationListItemView> rows) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Scarichi valorizzati");

            CellStyle headerStyle = createHeaderStyle(workbook);

            String[] headers = {
                    "Data",
                    "Articolo",
                    "Causale",
                    "Riferimento",
                    "Quantità",
                    "Costo unitario",
                    "Costo totale"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (OutboundValuationListItemView row : rows) {
                Row excelRow = sheet.createRow(rowIdx++);

                excelRow.createCell(0).setCellValue(nvl(row.getFormattedMovementDate()));
                excelRow.createCell(1).setCellValue(buildItemLabel(row));
                excelRow.createCell(2).setCellValue(nvl(row.getCausalCode()));
                excelRow.createCell(3).setCellValue(nvl(row.getReferenceLabel()));
                excelRow.createCell(4).setCellValue(nvl(row.getFormattedQuantity()));
                excelRow.createCell(5).setCellValue(nvl(row.getFormattedUnitCost()));
                excelRow.createCell(6).setCellValue(nvl(row.getFormattedTotalCost()));
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException ex) {
            throw new IllegalStateException("Errore nella generazione del file XLSX degli scarichi valorizzati.", ex);
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        return style;
    }

    private String buildItemLabel(OutboundValuationListItemView row) {
        String code = row.getItemCode() == null ? "" : row.getItemCode();
        String name = row.getItemName() == null ? "" : row.getItemName();
        return code + " - " + name;
    }

    private String nvl(String value) {
        return value == null ? "" : value;
    }
}
