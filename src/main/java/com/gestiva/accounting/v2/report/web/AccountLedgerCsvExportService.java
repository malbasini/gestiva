package com.gestiva.accounting.v2.report.web;

import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;

@Service
public class AccountLedgerCsvExportService {

    public byte[] export(AccountLedgerView ledger) {
        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF');
        sb.append("Conto;Dal;Al;Data;Numero;Causale;Descrizione;Dare;Avere;Saldo progressivo\n");

        if (ledger.getRows() != null) {
            String accountLabel = buildAccountLabel(ledger);

            for (AccountLedgerRowView row : ledger.getRows()) {
                sb.append(csv(accountLabel)).append(';')
                        .append(csv(ledger.getFormattedDateFrom())).append(';')
                        .append(csv(ledger.getFormattedDateTo())).append(';')
                        .append(csv(row.getFormattedEntryDate())).append(';')
                        .append(csv(row.getEntryNumber())).append(';')
                        .append(csv(row.getCausalCodeLabel())).append(';')
                        .append(csv(row.getDescription())).append(';')
                        .append(csv(row.getFormattedDebit())).append(';')
                        .append(csv(row.getFormattedCredit())).append(';')
                        .append(csv(row.getFormattedProgressiveBalance())).append('\n');
            }
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String buildAccountLabel(AccountLedgerView ledger) {
        String code = ledger.getAccountCode() == null ? "" : ledger.getAccountCode();
        String name = ledger.getAccountName() == null ? "" : ledger.getAccountName();
        return code.isBlank() ? name : code + " - " + name;
    }

    private String csv(String value) {
        if (value == null) {
            return "\"\"";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
