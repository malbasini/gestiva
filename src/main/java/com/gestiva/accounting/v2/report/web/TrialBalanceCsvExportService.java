package com.gestiva.accounting.v2.report.web;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class TrialBalanceCsvExportService {

    public byte[] export(TrialBalanceView trialBalance) {
        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF');
        sb.append("Dal;Al;Codice;Conto;Tipo;Natura;Dare;Avere;Saldo\n");

        if (trialBalance.getRows() != null) {
            for (TrialBalanceRowView row : trialBalance.getRows()) {
                sb.append(csv(trialBalance.getFormattedDateFrom())).append(';')
                        .append(csv(trialBalance.getFormattedDateTo())).append(';')
                        .append(csv(row.getAccountCode())).append(';')
                        .append(csv(row.getAccountName())).append(';')
                        .append(csv(row.getAccountType())).append(';')
                        .append(csv(row.getNature())).append(';')
                        .append(csv(row.getFormattedTotalDebit())).append(';')
                        .append(csv(row.getFormattedTotalCredit())).append(';')
                        .append(csv(row.getFormattedBalance())).append('\n');
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