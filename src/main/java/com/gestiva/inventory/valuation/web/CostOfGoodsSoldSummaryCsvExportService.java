package com.gestiva.inventory.valuation.web;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class CostOfGoodsSoldSummaryCsvExportService {

    public byte[] export(List<CostOfGoodsSoldSummaryItemView> rows) {
        StringBuilder sb = new StringBuilder();

        sb.append('\uFEFF');
        sb.append("Articolo;Quantità totale scaricata;Costo totale;Costo medio unitario\n");

        for (CostOfGoodsSoldSummaryItemView row : rows) {
            sb.append(csv(buildItemLabel(row))).append(';')
                    .append(csv(row.getFormattedTotalQuantity())).append(';')
                    .append(csv(row.getFormattedTotalCost())).append(';')
                    .append(csv(row.getFormattedAverageUnitCost())).append('\n');
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String buildItemLabel(CostOfGoodsSoldSummaryItemView row) {
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
