package com.gestiva.creditnote.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.creditnote.repository.CreditNoteLineRepository;
import com.gestiva.creditnote.repository.CreditNoteRepository;
import com.gestiva.purchasing.invoice.repository.SupplierInvoiceLineRepository;
import com.gestiva.purchasing.invoice.repository.SupplierInvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class SupplierCreditNoteRegisteredCreditsService {

    private final SupplierInvoiceRepository invoices;
    private final SupplierInvoiceLineRepository invoiceLines;
    private final CreditNoteRepository notes;
    private final CreditNoteLineRepository noteLines;

    public SupplierCreditNoteRegisteredCreditsService(
            SupplierInvoiceRepository invoices,
            SupplierInvoiceLineRepository invoiceLines,
            CreditNoteRepository notes,
            CreditNoteLineRepository noteLines) {

        this.invoices = invoices;
        this.invoiceLines = invoiceLines;
        this.notes = notes;
        this.noteLines = noteLines;
    }

    /**
     * Legge le rettifiche registrate sotto lock della fattura.
     *
     * Il chiamante che effettua una registrazione deve mantenere
     * lo stesso lock fino al commit. Questo metodo da solo
     * non registra e non autorizza nuove note di credito.
     */
    @Transactional
    public Map<Long, SupplierCreditNoteCumulativeCalculator.PreviousCredits>
    loadRegisteredCredits(
            Long tenantId,
            Long companyId,
            Long supplierInvoiceId) {

        if (tenantId == null || companyId == null
                || supplierInvoiceId == null) {
            throw new BusinessException(
                    "Identificativi obbligatori per il controllo rettifiche.");
        }

        var invoice = invoices
                .findByTenantIdAndCompanyIdAndIdForUpdate(
                        tenantId, companyId, supplierInvoiceId)
                .orElseThrow(() -> new BusinessException(
                        "Fattura originale non trovata nella società."));

        if (!"REGISTERED".equals(invoice.getStatus())) {
            throw new BusinessException(
                    "La fattura originale non è registrata.");
        }

        var originals = invoiceLines
                .findByTenantIdAndSupplierInvoiceIdOrderByLineNoAsc(
                        tenantId, supplierInvoiceId);

        Map<Long, BigDecimal> originalQty = new HashMap<>();
        Map<Long, BigDecimal> originalSubtotal = new HashMap<>();
        Map<Long, BigDecimal> originalTax = new HashMap<>();

        for (var line : originals) {
            if (line.getId() == null
                    || line.getQuantity() == null
                    || line.getLineSubtotal() == null
                    || line.getTaxAmount() == null
                    || line.getQuantity().signum() <= 0
                    || line.getLineSubtotal().signum() < 0
                    || line.getTaxAmount().signum() < 0) {
                throw new BusinessException(
                        "Riga della fattura originale non valida.");
            }

            originalQty.put(line.getId(), line.getQuantity());
            originalSubtotal.put(line.getId(), line.getLineSubtotal());
            originalTax.put(line.getId(), line.getTaxAmount());
        }

        var related = notes
                .findByTenantIdAndCompanyIdAndCreditNoteTypeAndOriginalInvoiceId(
                        tenantId, companyId, "SUPPLIER", supplierInvoiceId);

        Map<Long, BigDecimal> usedQty = new HashMap<>();
        Map<Long, BigDecimal> usedSubtotal = new HashMap<>();
        Map<Long, BigDecimal> usedTax = new HashMap<>();

        Set<Long> noteIds = new HashSet<>();

        for (var note : related) {
            if (!"REGISTERED".equals(note.getStatus())) {
                continue;
            }

            if (note.getId() == null || !noteIds.add(note.getId())) {
                throw new BusinessException(
                        "Nota di credito registrata non valida o duplicata.");
            }

            var lines = noteLines
                    .findByTenantIdAndCompanyIdAndCreditNoteIdOrderByLineNoAsc(
                            tenantId, companyId, note.getId());

            if (lines.isEmpty()) {
                throw new BusinessException(
                        "Nota di credito registrata senza righe.");
            }

            BigDecimal noteSubtotal = BigDecimal.ZERO;
            BigDecimal noteTax = BigDecimal.ZERO;
            BigDecimal noteTotal = BigDecimal.ZERO;

            for (var line : lines) {
                Long originalLineId = line.getOriginalInvoiceLineId();

                if (originalLineId == null
                        || !originalQty.containsKey(originalLineId)
                        || line.getQuantity() == null
                        || line.getLineSubtotal() == null
                        || line.getTaxAmount() == null
                        || line.getLineTotal() == null
                        || line.getQuantity().signum() <= 0
                        || line.getLineSubtotal().signum() < 0
                        || line.getTaxAmount().signum() < 0
                        || line.getLineSubtotal().add(line.getTaxAmount())
                                .compareTo(line.getLineTotal()) != 0) {
                    throw new BusinessException(
                            "Riga di nota registrata incoerente "
                            + "con la fattura originale.");
                }

                usedQty.merge(
                        originalLineId, line.getQuantity(), BigDecimal::add);
                usedSubtotal.merge(
                        originalLineId, line.getLineSubtotal(), BigDecimal::add);
                usedTax.merge(
                        originalLineId, line.getTaxAmount(), BigDecimal::add);

                noteSubtotal = noteSubtotal.add(line.getLineSubtotal());
                noteTax = noteTax.add(line.getTaxAmount());
                noteTotal = noteTotal.add(line.getLineTotal());
            }

            if (note.getSubtotalAmount() == null
                    || note.getTaxAmount() == null
                    || note.getTotalAmount() == null
                    || noteSubtotal.compareTo(note.getSubtotalAmount()) != 0
                    || noteTax.compareTo(note.getTaxAmount()) != 0
                    || noteTotal.compareTo(note.getTotalAmount()) != 0) {
                throw new BusinessException(
                        "Totali della nota registrata non quadrati.");
            }
        }

        Map<Long, SupplierCreditNoteCumulativeCalculator.PreviousCredits>
                result = new HashMap<>();

        for (Long id : originalQty.keySet()) {
            BigDecimal qty = usedQty.getOrDefault(id, BigDecimal.ZERO);
            BigDecimal subtotal = usedSubtotal.getOrDefault(
                    id, BigDecimal.ZERO);
            BigDecimal tax = usedTax.getOrDefault(id, BigDecimal.ZERO);

            if (qty.compareTo(originalQty.get(id)) > 0
                    || subtotal.compareTo(originalSubtotal.get(id)) > 0
                    || tax.compareTo(originalTax.get(id)) > 0) {
                throw new BusinessException(
                        "Rettifiche registrate superiori alla fattura.");
            }

            result.put(
                    id,
                    new SupplierCreditNoteCumulativeCalculator.PreviousCredits(
                            qty, subtotal, tax));
        }

        return Map.copyOf(result);
    }
}
