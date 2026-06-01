package com.gestiva.accounting.v2.report.web;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class BalanceSheetCsvExportService {

    public byte[] export(BalanceSheetView view) {
        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF');
        sb.append("Dal;Al;Sezione;Codice;Conto;Importo\n");

        if (view.getSections() != null) {
            for (BalanceSheetSectionView section : view.getSections()) {
                if (section.getRows() == null) {
                    continue;
                }

                for (BalanceSheetRowView row : section.getRows()) {
                    sb.append(csv(view.getFormattedDateFrom())).append(';')
                            .append(csv(view.getFormattedDateTo())).append(';')
                            .append(csv(section.getSectionLabel())).append(';')
                            .append(csv(row.getAccountCode())).append(';')
                            .append(csv(row.getAccountName())).append(';')
                            .append(csv(row.getFormattedAmount())).append('\n');
                }
            }
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String csv(String value) {
        if (value == null) {
            return "\"\"";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}