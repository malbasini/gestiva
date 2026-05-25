package com.gestiva.accounting.vat.web;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class VatSettlementCsvExportService {

    public byte[] export(VatSettlementView settlement) {
        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF');
        sb.append("Dal;Al;Imponibile vendite;IVA vendite;Imponibile acquisti;IVA acquisti;Saldo IVA;Esito\n");

        sb.append(csv(settlement.getFormattedDateFrom())).append(';')
                .append(csv(settlement.getFormattedDateTo())).append(';')
                .append(csv(settlement.getFormattedSalesTaxableAmount())).append(';')
                .append(csv(settlement.getFormattedSalesTaxAmount())).append(';')
                .append(csv(settlement.getFormattedPurchaseTaxableAmount())).append(';')
                .append(csv(settlement.getFormattedPurchaseTaxAmount())).append(';')
                .append(csv(settlement.getFormattedVatBalance())).append(';')
                .append(csv(settlement.getBalanceTypeLabel())).append('\n');

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String csv(String value) {
        if (value == null) {
            return "\"\"";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}