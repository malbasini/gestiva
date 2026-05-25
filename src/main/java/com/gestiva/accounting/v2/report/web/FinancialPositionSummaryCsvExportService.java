package com.gestiva.accounting.v2.report.web;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class FinancialPositionSummaryCsvExportService {

    public byte[] export(FinancialPositionSummaryView summary) {
        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF');
        sb.append("Dal;Al;Classe;Saldo\n");

        if (summary.getRows() != null) {
            for (FinancialPositionSummaryRowView row : summary.getRows()) {
                sb.append(csv(summary.getFormattedDateFrom())).append(';')
                        .append(csv(summary.getFormattedDateTo())).append(';')
                        .append(csv(row.getAccountTypeLabel())).append(';')
                        .append(csv(row.getFormattedAmount())).append('\n');
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