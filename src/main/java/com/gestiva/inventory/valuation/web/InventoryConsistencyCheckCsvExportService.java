package com.gestiva.inventory.valuation.web;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class InventoryConsistencyCheckCsvExportService {

    public byte[] export(List<InventoryConsistencyCheckItemView> rows) {
        StringBuilder sb = new StringBuilder();

        sb.append('\uFEFF');
        sb.append("Articolo;Giacenza ledger;Giacenza valorizzata;Differenza;Stato\n");

        for (InventoryConsistencyCheckItemView row : rows) {
            sb.append(csv(buildItemLabel(row))).append(';')
                    .append(csv(row.getFormattedLedgerQty())).append(';')
                    .append(csv(row.getFormattedLayerQty())).append(';')
                    .append(csv(row.getFormattedDifferenceQty())).append(';')
                    .append(csv(row.getStatusLabel())).append('\n');
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String buildItemLabel(InventoryConsistencyCheckItemView row) {
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