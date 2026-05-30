package com.gestiva.accounting.payment.web;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import org.springframework.data.domain.Page;

@Service
public class PaymentTransactionCsvExportService {

    public byte[] export(Page<PaymentTransactionListItemView> page) {
        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF');
        sb.append("Data;Tipo;Soggetto;Documento;Importo;Metodo;Riferimento;Stato scadenza\n");

        if (page != null && page.getContent() != null) {
            for (PaymentTransactionListItemView row : page.getContent()) {
                sb.append(csv(row.getFormattedPaymentDate())).append(';')
                        .append(csv(row.getDirectionLabel())).append(';')
                        .append(csv(row.getPartyLabel())).append(';')
                        .append(csv(row.getDocumentLabel())).append(';')
                        .append(csv(row.getFormattedAmount())).append(';')
                        .append(csv(row.getPaymentMethod())).append(';')
                        .append(csv(row.getReference())).append(';')
                        .append(csv(resolveDueStatusLabel(row.getDueStatus()))).append('\n');
            }
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String resolveDueStatusLabel(String status) {
        if (status == null) return "";
        return switch (status.toUpperCase()) {
            case "PAID" -> "SALDATA";
            case "PARTIALLY_PAID" -> "PARZIALMENTE SALDATA";
            case "CANCELLED" -> "ANNULLATA";
            case "OPEN" -> "APERTA";
            default -> status;
        };
    }

    private String csv(String value) {
        if (value == null) {
            return "\"\"";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
