package com.gestiva.purchasing.invoice.service;

import com.gestiva.common.exception.BusinessException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Piano immutabile di una futura fattura fornitore
 * generata da ricevimento parziale.
 *
 * Non persiste documenti e non assegna numeratori.
 * Gli importi delle righe provengono dall'allocazione
 * cumulativa, non dal costo FIFO.
 */
public record SupplierInvoicePartialDraftPlan(
        Long tenantId,
        Long companyId,
        Long goodsReceiptId,
        Long purchaseOrderId,
        Long supplierId,
        String currencyCode,
        List<Line> lines,
        BigDecimal subtotal,
        BigDecimal tax,
        BigDecimal total
) {

    private static final BigDecimal MAX_AMOUNT =
            new BigDecimal("9999999999999.99");

    public record Line(
            Integer lineNo,
            Long purchaseOrderLineId,
            Long itemId,
            String description,
            Long uomId,
            String uomCode,
            String baseUomCode,
            BigDecimal conversionFactor,
            BigDecimal commercialQuantity,
            BigDecimal unitPrice,
            BigDecimal discountPct,
            BigDecimal taxPct,
            BigDecimal subtotal,
            BigDecimal tax,
            BigDecimal total
    ) {
        public Line {
            if (lineNo == null || lineNo <= 0
                    || purchaseOrderLineId == null
                    || purchaseOrderLineId <= 0
                    || itemId == null || itemId <= 0
                    || description == null
                    || description.isBlank()
                    || description.length() > 500
                    || uomId == null || uomId <= 0
                    || uomCode == null || uomCode.isBlank()
                    || uomCode.length() > 20
                    || baseUomCode == null
                    || baseUomCode.isBlank()
                    || baseUomCode.length() > 20) {
                throw new BusinessException(
                        "Dati identificativi della riga "
                        + "fattura parziale non validi."
                );
            }

            commercialQuantity = positiveDecimal(
                    commercialQuantity, 3, 12,
                    "Quantità commerciale"
            );

            conversionFactor = positiveDecimal(
                    conversionFactor, 6, 13,
                    "Fattore conversione"
            );

            unitPrice = nonNegativeDecimal(
                    unitPrice, 2, 13, "Prezzo unitario"
            );

            discountPct = nonNegativeDecimal(
                    discountPct, 2, 4, "Sconto percentuale"
            );

            taxPct = nonNegativeDecimal(
                    taxPct, 2, 4, "Aliquota IVA"
            );

            if (discountPct.compareTo(
                    new BigDecimal("100.00")) > 0) {
                throw new BusinessException(
                        "Sconto superiore al 100%."
                );
            }

            subtotal = nonNegativeDecimal(
                    subtotal, 2, 13, "Imponibile riga"
            );

            tax = nonNegativeDecimal(
                    tax, 2, 13, "IVA riga"
            );

            total = nonNegativeDecimal(
                    total, 2, 13, "Totale riga"
            );

            if (subtotal.signum() <= 0
                    || total.signum() <= 0
                    || subtotal.add(tax)
                            .compareTo(total) != 0) {
                throw new BusinessException(
                        "Importi della riga fattura "
                        + "non coerenti."
                );
            }

            // Non ricalcoliamo gli importi ripartiti
            // con quantity * unitPrice: l'allocazione
            // cumulativa può includere arrotondamenti.
        }
    }

    public SupplierInvoicePartialDraftPlan {
        if (tenantId == null || tenantId <= 0
                || companyId == null || companyId <= 0
                || goodsReceiptId == null
                || goodsReceiptId <= 0
                || purchaseOrderId == null
                || purchaseOrderId <= 0
                || supplierId == null || supplierId <= 0
                || currencyCode == null
                || !currencyCode.matches("[A-Z]{3}")) {
            throw new BusinessException(
                    "Intestazione piano fattura parziale "
                    + "non valida."
            );
        }

        if (lines == null || lines.isEmpty()) {
            throw new BusinessException(
                    "Piano fattura parziale senza righe."
            );
        }

        lines = List.copyOf(lines);

        Set<Long> orderLineIds = new HashSet<>();
        BigDecimal calculatedSubtotal = BigDecimal.ZERO;
        BigDecimal calculatedTax = BigDecimal.ZERO;
        BigDecimal calculatedTotal = BigDecimal.ZERO;

        int expectedLineNo = 1;

        for (Line line : lines) {
            if (line == null
                    || line.lineNo() != expectedLineNo++
                    || !orderLineIds.add(
                            line.purchaseOrderLineId())) {
                throw new BusinessException(
                        "Righe del piano fattura duplicate "
                        + "o non ordinate."
                );
            }

            calculatedSubtotal = calculatedSubtotal.add(
                    line.subtotal()
            );
            calculatedTax = calculatedTax.add(line.tax());
            calculatedTotal = calculatedTotal.add(
                    line.total()
            );
        }

        subtotal = nonNegativeDecimal(
                subtotal, 2, 13, "Imponibile fattura"
        );
        tax = nonNegativeDecimal(
                tax, 2, 13, "IVA fattura"
        );
        total = nonNegativeDecimal(
                total, 2, 13, "Totale fattura"
        );

        if (subtotal.signum() <= 0
                || total.signum() <= 0
                || subtotal.compareTo(calculatedSubtotal) != 0
                || tax.compareTo(calculatedTax) != 0
                || total.compareTo(calculatedTotal) != 0
                || subtotal.add(tax).compareTo(total) != 0) {
            throw new BusinessException(
                    "Totali del piano fattura parziale "
                    + "non quadrati con le righe."
            );
        }
    }

    private static BigDecimal positiveDecimal(
            BigDecimal value,
            int scale,
            int maxIntegerDigits,
            String field) {

        BigDecimal result = nonNegativeDecimal(
                value, scale, maxIntegerDigits, field
        );

        if (result.signum() <= 0) {
            throw new BusinessException(
                    field + " deve essere positivo."
            );
        }

        return result;
    }

    private static BigDecimal nonNegativeDecimal(
            BigDecimal value,
            int scale,
            int maxIntegerDigits,
            String field) {

        if (value == null) {
            throw new BusinessException(
                    field + " mancante."
            );
        }

        final BigDecimal normalized;

        try {
            normalized = value.setScale(
                    scale, RoundingMode.UNNECESSARY
            );
        } catch (ArithmeticException ex) {
            throw new BusinessException(
                    field + ": precisione non valida."
            );
        }

        if (normalized.signum() < 0
                || normalized.precision()
                        - normalized.scale()
                        > maxIntegerDigits
                || (scale == 2
                    && maxIntegerDigits == 13
                    && normalized.compareTo(MAX_AMOUNT) > 0)) {
            throw new BusinessException(
                    field + ": valore fuori intervallo."
            );
        }

        return normalized;
    }
}
