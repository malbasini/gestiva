package com.gestiva.accounting.v2.report.web;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class FinancialPositionDetailCsvExportService {

    public byte[] export(FinancialPositionDetailView view) {
        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF');
        sb.append("Dal;Al;Classe;Codice;Conto;Saldo\n");

        if (view.getSections() != null) {
            for (FinancialPositionSectionView section : view.getSections()) {
                if (section.getDetails() == null) {
                    continue;
                }

                for (FinancialPositionDetailRowView detail : section.getDetails()) {
                    sb.append(csv(view.getFormattedDateFrom())).append(';')
                            .append(csv(view.getFormattedDateTo())).append(';')
                            .append(csv(section.getAccountTypeLabel())).append(';')
                            .append(csv(detail.getAccountCode())).append(';')
                            .append(csv(detail.getAccountName())).append(';')
                            .append(csv(detail.getFormattedAmount())).append('\n');
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
