package com.gestiva.accounting.vat.web;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class VatSalesRegisterCsvExportService {

    public byte[] export(List<VatSalesRegisterRowView> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF');
        sb.append("Data;Numero fattura;Cliente;Aliquota;Imponibile;IVA;Totale\n");

        for (VatSalesRegisterRowView row : rows) {
            sb.append(csv(row.getFormattedInvoiceDate())).append(';')
                    .append(csv(row.getInvoiceNumber())).append(';')
                    .append(csv(row.getCustomerName())).append(';')
                    .append(csv(row.getFormattedTaxPct())).append(';')
                    .append(csv(row.getFormattedTaxableAmount())).append(';')
                    .append(csv(row.getFormattedTaxAmount())).append(';')
                    .append(csv(row.getFormattedTotalAmount())).append('\n');
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