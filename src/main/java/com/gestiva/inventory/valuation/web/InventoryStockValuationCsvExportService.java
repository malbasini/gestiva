package com.gestiva.inventory.valuation.web;

import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class InventoryStockValuationCsvExportService {

    public byte[] export(List<InventoryStockValuationListItemView> rows) {
        StringBuilder sb = new StringBuilder();

        sb.append('\uFEFF');
        sb.append("Articolo;Giacenza attuale;Valore magazzino;Costo medio residuo;Layer aperti\n");

        for (InventoryStockValuationListItemView row : rows) {
            sb.append(csv(buildItemLabel(row))).append(';')
                    .append(csv(row.getFormattedCurrentQty())).append(';')
                    .append(csv(row.getFormattedInventoryValue())).append(';')
                    .append(csv(row.getFormattedAverageResidualCost())).append(';')
                    .append(csv(String.valueOf(row.getOpenLayerCount()))).append('\n');
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String buildItemLabel(InventoryStockValuationListItemView row) {
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