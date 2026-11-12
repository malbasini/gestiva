
package com.gestiva.creditnote.service;

import com.gestiva.billing.invoice.entity.Invoice;
import com.gestiva.billing.invoice.entity.InvoiceLine;
import com.gestiva.common.exception.BusinessException;
import com.gestiva.creditnote.entity.CreditNote;
import com.gestiva.creditnote.entity.CreditNoteLine;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class CustomerCreditNoteRegistrationValidator {

    private CustomerCreditNoteRegistrationValidator() {
    }

    public static void validate(
            Invoice invoice,
            CreditNote draft,
            List<InvoiceLine> originals,
            List<CreditNoteLine> draftLines,
            Map<Long,
                    CustomerCreditNoteCumulativeCalculator.PreviousCredits>
                    previousCredits) {

        if (invoice == null
                || draft == null
                || originals == null
                || draftLines == null
                || previousCredits == null) {
            throw new BusinessException(
                    "Dati validazione nota cliente mancanti.");
        }

        if (!"ISSUED".equals(invoice.getStatus())
                || !"DRAFT".equals(draft.getStatus())
                || !"CUSTOMER".equals(draft.getCreditNoteType())
                || draft.getId() == null
                || !Objects.equals(
                invoice.getId(), draft.getOriginalInvoiceId())
                || !Objects.equals(
                invoice.getTenantId(), draft.getTenantId())
                || !Objects.equals(
                invoice.getCompanyId(), draft.getCompanyId())
                || !Objects.equals(
                invoice.getCustomerId(), draft.getPartyId())
                || !Objects.equals(
                invoice.getCurrencyCode(), draft.getCurrencyCode())) {
            throw new BusinessException(
                    "Bozza cliente non coerente con la fattura originale.");
        }

        if (originals.isEmpty() || draftLines.isEmpty()) {
            throw new BusinessException(
                    "Righe originali o righe nota cliente mancanti.");
        }

        Map<Long, InvoiceLine> originalById = new HashMap<>();

        for (InvoiceLine original : originals) {

            if (original.getId() == null
                    || !Objects.equals(
                    original.getTenantId(), invoice.getTenantId())
                    || !Objects.equals(
                    original.getInvoiceId(), invoice.getId())
                    || originalById.putIfAbsent(
                    original.getId(), original) != null) {
                throw new BusinessException(
                        "Righe della fattura cliente non valide.");
            }
        }

        if (!originalById.keySet().containsAll(
                previousCredits.keySet())) {
            throw new BusinessException(
                    "Rettifiche precedenti relative a righe estranee.");
        }

        Set<Long> selectedIds = new HashSet<>();

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;

        for (CreditNoteLine line : draftLines) {

            if (line == null) {
                throw new BusinessException(
                        "Riga nota cliente mancante.");
            }

            Long originalId = line.getOriginalInvoiceLineId();
            InvoiceLine original = originalById.get(originalId);

            if (original == null
                    || !selectedIds.add(originalId)
                    || !Objects.equals(
                    line.getTenantId(), draft.getTenantId())
                    || !Objects.equals(
                    line.getCompanyId(), draft.getCompanyId())
                    || !Objects.equals(
                    line.getCreditNoteId(), draft.getId())
                    || !Objects.equals(
                    line.getItemId(), original.getItemId())
                    || !Objects.equals(
                    line.getTaxPct(), original.getTaxPct())
                    || !Objects.equals(
                    line.getUnitOfMeasure(),
                    original.getUnitOfMeasure())
                    || !Objects.equals(
                    line.getUomId(), original.getUomId())
                    || !Objects.equals(
                    line.getUomCode(),
                    original.getUnitOfMeasure())
                    || !Objects.equals(
                    line.getBaseUomCode(),
                    original.getBaseUomCode())
                    || !Objects.equals(
                    line.getConversionFactor(),
                    original.getConversionFactor())
                    || !Objects.equals(
                    line.getUnitPrice(), original.getUnitPrice())
                    || !Objects.equals(
                    line.getDiscountPct(),
                    original.getDiscountPct())
                    || !Objects.equals(
                    line.getDescription(),
                    original.getDescription())
                    || !Objects.equals(
                    line.getLineNo(), original.getLineNo())) {
                throw new BusinessException(
                        "Riga della nota cliente non coerente "
                                + "con la fattura originale.");
            }

            var previous = previousCredits.getOrDefault(
                    originalId,
                    new CustomerCreditNoteCumulativeCalculator.PreviousCredits(
                            BigDecimal.ZERO,
                            BigDecimal.ZERO,
                            BigDecimal.ZERO));

            var expected =
                    CustomerCreditNoteCumulativeCalculator.calculate(
                            original,
                            previous,
                            line.getQuantity());

            if (!same(expected.subtotal(), line.getLineSubtotal())
                    || !same(expected.tax(), line.getTaxAmount())
                    || !same(expected.total(), line.getLineTotal())) {
                throw new BusinessException(
                        "Importi della nota cliente non più "
                                + "compatibili con il residuo disponibile.");
            }

            subtotal = subtotal.add(expected.subtotal());
            tax = tax.add(expected.tax());
            total = total.add(expected.total());
        }

        if (!same(subtotal, draft.getSubtotalAmount())
                || !same(tax, draft.getTaxAmount())
                || !same(total, draft.getTotalAmount())
                || total.signum() <= 0) {
            throw new BusinessException(
                    "Totali della nota cliente non quadrati.");
        }
    }

    private static boolean same(BigDecimal a, BigDecimal b) {
        return a != null
                && b != null
                && a.compareTo(b) == 0;
    }
}
