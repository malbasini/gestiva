package com.gestiva.inventory.valuation.web;

import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class OutboundValuationCsvExportService {

    public byte[] export(List<OutboundValuationListItemView> rows) {
        StringBuilder sb = new StringBuilder();

        sb.append('\uFEFF');
        sb.append("Data;Articolo;Causale;Riferimento;Quantità;Costo unitario;Costo totale\n");

        for (OutboundValuationListItemView row : rows) {
            sb.append(csv(row.getFormattedMovementDate())).append(';')
                    .append(csv(buildItemLabel(row))).append(';')
                    .append(csv(row.getCausalCode())).append(';')
                    .append(csv(row.getReferenceLabel())).append(';')
                    .append(csv(row.getFormattedQuantity())).append(';')
                    .append(csv(row.getFormattedUnitCost())).append(';')
                    .append(csv(row.getFormattedTotalCost())).append('\n');
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String buildItemLabel(OutboundValuationListItemView row) {
        String code = row.getItemCode() == null ? "" : row.getItemCode();
        String name = row.getItemName() == null ? "" : row.getItemName();
        return code + " - " + name;
    }

    private String csv(String value) {
        if (value == null) {
            return "\"\"";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}