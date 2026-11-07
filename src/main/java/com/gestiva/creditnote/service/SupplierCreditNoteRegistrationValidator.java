package com.gestiva.creditnote.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.creditnote.entity.CreditNote;
import com.gestiva.creditnote.entity.CreditNoteLine;
import com.gestiva.purchasing.invoice.entity.SupplierInvoice;
import com.gestiva.purchasing.invoice.entity.SupplierInvoiceLine;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class SupplierCreditNoteRegistrationValidator {

    private SupplierCreditNoteRegistrationValidator() {
    }

    public static void validate(
            SupplierInvoice invoice,
            CreditNote draft,
            List<SupplierInvoiceLine> originals,
            List<CreditNoteLine> draftLines,
            Map<Long, SupplierCreditNoteCumulativeCalculator.PreviousCredits>
                    previousCredits) {

        if (invoice == null || draft == null
                || originals == null || draftLines == null
                || previousCredits == null) {
            throw new BusinessException("Dati di validazione mancanti.");
        }

        if (!"REGISTERED".equals(invoice.getStatus())
                || !"DRAFT".equals(draft.getStatus())
                || !"SUPPLIER".equals(draft.getCreditNoteType())
                || !Objects.equals(invoice.getId(),
                        draft.getOriginalInvoiceId())
                || !Objects.equals(invoice.getTenantId(),
                        draft.getTenantId())
                || !Objects.equals(invoice.getCompanyId(),
                        draft.getCompanyId())
                || !Objects.equals(invoice.getSupplierId(),
                        draft.getPartyId())
                || !Objects.equals(invoice.getCurrencyCode(),
                        draft.getCurrencyCode())) {
            throw new BusinessException(
                    "Bozza non coerente con la fattura originale.");
        }

        if (originals.isEmpty() || draftLines.isEmpty()) {
            throw new BusinessException("Righe originali o bozza mancanti.");
        }

        Map<Long, SupplierInvoiceLine> originalById = new HashMap<>();

        for (var original : originals) {
            if (original.getId() == null
                    || originalById.putIfAbsent(
                            original.getId(), original) != null) {
                throw new BusinessException(
                        "Righe della fattura originale duplicate o non valide.");
            }
        }

        if (!originalById.keySet().containsAll(previousCredits.keySet())) {
            throw new BusinessException(
                    "Rettifiche precedenti relative a righe estranee.");
        }

        Set<Long> selectedIds = new HashSet<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;

        for (CreditNoteLine line : draftLines) {
            Long originalId = line.getOriginalInvoiceLineId();
            SupplierInvoiceLine original = originalById.get(originalId);

            if (original == null || !selectedIds.add(originalId)
                    || !Objects.equals(line.getTenantId(),
                            draft.getTenantId())
                    || !Objects.equals(line.getCompanyId(),
                            draft.getCompanyId())
                    || !Objects.equals(line.getCreditNoteId(),
                            draft.getId())
                    || !Objects.equals(line.getItemId(),
                            original.getItemId())
                    || !Objects.equals(line.getTaxPct(),
                            original.getTaxPct())
                    || !Objects.equals(line.getUomCode(),
                            original.getUomCode())
                    || !Objects.equals(line.getBaseUomCode(),
                            original.getBaseUomCode())
                    || !Objects.equals(line.getConversionFactor(),
                            original.getConversionFactor())) {
                throw new BusinessException(
                        "Riga della bozza non coerente con l'originale.");
            }

            var previous = previousCredits.getOrDefault(
                    originalId,
                    new SupplierCreditNoteCumulativeCalculator.PreviousCredits(
                            BigDecimal.ZERO,
                            BigDecimal.ZERO,
                            BigDecimal.ZERO));

            var expected = SupplierCreditNoteCumulativeCalculator.calculate(
                    original, previous, line.getQuantity());

            if (!same(expected.subtotal(), line.getLineSubtotal())
                    || !same(expected.tax(), line.getTaxAmount())
                    || !same(expected.total(), line.getLineTotal())) {
                throw new BusinessException(
                        "Importi della bozza non più compatibili "
                        + "con i residui disponibili: ricalcolo necessario.");
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
                    "Totali della nota di credito non quadrati.");
        }
    }

    private static boolean same(BigDecimal a, BigDecimal b) {
        return a != null && b != null && a.compareTo(b) == 0;
    }
}
