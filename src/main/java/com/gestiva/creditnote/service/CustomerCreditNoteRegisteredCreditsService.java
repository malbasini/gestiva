
package com.gestiva.creditnote.service;

import com.gestiva.billing.invoice.repository.InvoiceLineRepository;
import com.gestiva.billing.invoice.repository.InvoiceRepository;
import com.gestiva.common.exception.BusinessException;
import com.gestiva.creditnote.repository.CreditNoteLineRepository;
import com.gestiva.creditnote.repository.CreditNoteRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class CustomerCreditNoteRegisteredCreditsService {

    private final InvoiceRepository invoices;
    private final InvoiceLineRepository invoiceLines;
    private final CreditNoteRepository notes;
    private final CreditNoteLineRepository noteLines;

    public CustomerCreditNoteRegisteredCreditsService(
            InvoiceRepository invoices,
            InvoiceLineRepository invoiceLines,
            CreditNoteRepository notes,
            CreditNoteLineRepository noteLines) {

        this.invoices = invoices;
        this.invoiceLines = invoiceLines;
        this.notes = notes;
        this.noteLines = noteLines;
    }

    /**
     * Legge le rettifiche CUSTOMER già REGISTERED.
     *
     * Acquisisce un lock pessimistico sulla fattura.
     *
     * Il chiamante che effettua una registrazione
     * deve mantenere la stessa transazione fino al commit.
     *
     * Le note DRAFT non consumano residuo.
     */
    @Transactional
    public Map<Long,
            CustomerCreditNoteCumulativeCalculator.PreviousCredits>
    loadRegisteredCredits(
            Long tenantId,
            Long companyId,
            Long customerInvoiceId) {

        if (tenantId == null
                || companyId == null
                || customerInvoiceId == null) {
            throw new BusinessException(
                    "Identificativi mancanti per il controllo rettifiche.");
        }

        var invoice = invoices
                .findByTenantIdAndCompanyIdAndIdForUpdate(
                        tenantId, companyId, customerInvoiceId)
                .orElseThrow(() -> new BusinessException(
                        "Fattura cliente non trovata nella società attiva."));

        if (!"ISSUED".equals(invoice.getStatus())) {
            throw new BusinessException(
                    "La fattura cliente deve essere ISSUED.");
        }

        var originals = invoiceLines
                .findByTenantIdAndInvoiceIdOrderByLineNoAsc(
                        tenantId, customerInvoiceId);

        if (originals.isEmpty()) {
            throw new BusinessException(
                    "Fattura cliente senza righe.");
        }

        Map<Long, BigDecimal> originalQty = new HashMap<>();
        Map<Long, BigDecimal> originalSubtotal = new HashMap<>();
        Map<Long, BigDecimal> originalTax = new HashMap<>();

        for (var line : originals) {

            BigDecimal qty = line.getQuantity();
            BigDecimal total = line.getLineTotal();
            BigDecimal tax = line.getTaxAmount();

            if (line.getId() == null
                    || !tenantId.equals(line.getTenantId())
                    || !customerInvoiceId.equals(line.getInvoiceId())
                    || qty == null
                    || total == null
                    || tax == null
                    || qty.signum() <= 0
                    || qty.scale() > 3
                    || total.signum() <= 0
                    || tax.signum() < 0
                    || tax.compareTo(total) > 0
                    || originalQty.containsKey(line.getId())) {
                throw new BusinessException(
                        "Riga fattura cliente non valida o duplicata.");
            }

            originalQty.put(line.getId(), qty);
            // InvoiceLine.lineTotal è l'imponibile IVA esclusa.
            originalSubtotal.put(
                    line.getId(), total);
            originalTax.put(line.getId(), tax);
        }

        var related = notes
                .findByTenantIdAndCompanyIdAndCreditNoteTypeAndOriginalInvoiceId(
                        tenantId,
                        companyId,
                        "CUSTOMER",
                        customerInvoiceId);

        Map<Long, BigDecimal> usedQty = new HashMap<>();
        Map<Long, BigDecimal> usedSubtotal = new HashMap<>();
        Map<Long, BigDecimal> usedTax = new HashMap<>();

        Set<Long> noteIds = new HashSet<>();

        for (var note : related) {

            if (!"REGISTERED".equals(note.getStatus())) {
                continue;
            }

            if (note.getId() == null
                    || !noteIds.add(note.getId())
                    || !tenantId.equals(note.getTenantId())
                    || !companyId.equals(note.getCompanyId())
                    || !customerInvoiceId.equals(
                    note.getOriginalInvoiceId())
                    || !"CUSTOMER".equals(note.getCreditNoteType())
                    || !java.util.Objects.equals(
                    invoice.getCustomerId(), note.getPartyId())
                    || !java.util.Objects.equals(
                    invoice.getCurrencyCode(),
                    note.getCurrencyCode())) {
                throw new BusinessException(
                        "Nota cliente registrata incoerente.");
            }

            var lines = noteLines
                    .findByTenantIdAndCompanyIdAndCreditNoteIdOrderByLineNoAsc(
                            tenantId, companyId, note.getId());

            if (lines.isEmpty()) {
                throw new BusinessException(
                        "Nota cliente registrata senza righe.");
            }

            BigDecimal noteSubtotal = BigDecimal.ZERO;
            BigDecimal noteTax = BigDecimal.ZERO;
            BigDecimal noteTotal = BigDecimal.ZERO;

            Set<Long> seenLines = new HashSet<>();

            for (var line : lines) {

                Long originalLineId =
                        line.getOriginalInvoiceLineId();

                if (originalLineId == null
                        || !originalQty.containsKey(originalLineId)
                        || !seenLines.add(originalLineId)
                        || !tenantId.equals(line.getTenantId())
                        || !companyId.equals(line.getCompanyId())
                        || !note.getId().equals(
                        line.getCreditNoteId())
                        || line.getQuantity() == null
                        || line.getLineSubtotal() == null
                        || line.getTaxAmount() == null
                        || line.getLineTotal() == null
                        || line.getQuantity().signum() <= 0
                        || line.getQuantity().scale() > 3
                        || line.getLineSubtotal().signum() < 0
                        || line.getTaxAmount().signum() < 0
                        || line.getLineTotal().signum() <= 0
                        || line.getLineSubtotal()
                        .add(line.getTaxAmount())
                        .compareTo(line.getLineTotal()) != 0) {
                    throw new BusinessException(
                            "Riga nota cliente registrata incoerente.");
                }

                usedQty.merge(
                        originalLineId,
                        line.getQuantity(),
                        BigDecimal::add);

                usedSubtotal.merge(
                        originalLineId,
                        line.getLineSubtotal(),
                        BigDecimal::add);

                usedTax.merge(
                        originalLineId,
                        line.getTaxAmount(),
                        BigDecimal::add);

                noteSubtotal = noteSubtotal.add(
                        line.getLineSubtotal());

                noteTax = noteTax.add(
                        line.getTaxAmount());

                noteTotal = noteTotal.add(
                        line.getLineTotal());
            }

            if (note.getSubtotalAmount() == null
                    || note.getTaxAmount() == null
                    || note.getTotalAmount() == null
                    || noteSubtotal.compareTo(
                    note.getSubtotalAmount()) != 0
                    || noteTax.compareTo(
                    note.getTaxAmount()) != 0
                    || noteTotal.compareTo(
                    note.getTotalAmount()) != 0) {
                throw new BusinessException(
                        "Totali della nota cliente registrata non quadrati.");
            }
        }

        Map<Long,
                CustomerCreditNoteCumulativeCalculator.PreviousCredits>
                result = new HashMap<>();

        for (Long originalId : originalQty.keySet()) {

            BigDecimal qty = usedQty.getOrDefault(
                    originalId, BigDecimal.ZERO);

            BigDecimal subtotal = usedSubtotal.getOrDefault(
                    originalId, BigDecimal.ZERO);

            BigDecimal tax = usedTax.getOrDefault(
                    originalId, BigDecimal.ZERO);

            if (qty.compareTo(originalQty.get(originalId)) > 0
                    || subtotal.compareTo(
                    originalSubtotal.get(originalId)) > 0
                    || tax.compareTo(
                    originalTax.get(originalId)) > 0) {
                throw new BusinessException(
                        "Rettifiche clienti superiori alla fattura.");
            }

            result.put(
                    originalId,
                    new CustomerCreditNoteCumulativeCalculator.PreviousCredits(
                            qty, subtotal, tax));
        }

        return Map.copyOf(result);
    }
}
