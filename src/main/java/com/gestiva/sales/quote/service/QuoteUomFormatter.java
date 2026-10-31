package com.gestiva.sales.quote.service;

import com.gestiva.documents.pdf.PdfFormatUtils;
import com.gestiva.sales.quote.entity.QuoteLine;

import java.math.BigDecimal;

public final class QuoteUomFormatter {

    private QuoteUomFormatter() {
    }

    public static String format(QuoteLine line) {
        if (line == null || line.getQuantity() == null) {
            return "";
        }

        String quantity = PdfFormatUtils.formatDecimalTrimmed(
                line.getQuantity(), 2
        );

        String commercialUom = line.getUnitOfMeasure();

        if (commercialUom == null || commercialUom.isBlank()) {
            return quantity;
        }

        String result = quantity + " " + commercialUom.trim();

        BigDecimal factor = line.getConversionFactor();
        String baseUom = line.getBaseUomCode();

        if (factor == null
                || factor.compareTo(BigDecimal.ZERO) <= 0
                || baseUom == null
                || baseUom.isBlank()
                || commercialUom.trim().equalsIgnoreCase(baseUom.trim())) {
            return result;
        }

        BigDecimal baseQuantity = line.getQuantity().multiply(factor);

        return result + " ("
                + PdfFormatUtils.formatDecimalTrimmed(baseQuantity, 3)
                + " " + baseUom.trim() + ")";
    }
}
