
package com.gestiva.creditnote.service;

import com.gestiva.billing.invoice.repository.InvoiceLineRepository;
import com.gestiva.billing.invoice.repository.InvoiceRepository;
import com.gestiva.common.exception.BusinessException;
import com.gestiva.creditnote.entity.CreditNote;
import com.gestiva.creditnote.entity.CreditNoteLine;
import com.gestiva.creditnote.repository.CreditNoteLineRepository;
import com.gestiva.creditnote.repository.CreditNoteRepository;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.settings.sequence.service.DocumentSequenceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;



@Service
public class CustomerCreditNoteDraftService {

    private final InvoiceRepository invoices;
    private final InvoiceLineRepository invoiceLines;
    private final CreditNoteRepository notes;
    private final CreditNoteLineRepository noteLines;
    private final DocumentSequenceService sequences;
    private final CompanyContext companyContext;
    private final CustomerCreditNoteRegisteredCreditsService registeredCredits;

    public CustomerCreditNoteDraftService(
            InvoiceRepository invoices,
            InvoiceLineRepository invoiceLines,
            CreditNoteRepository notes,
            CreditNoteLineRepository noteLines,
            DocumentSequenceService sequences,
            CompanyContext companyContext,
            CustomerCreditNoteRegisteredCreditsService registeredCredits) {

        this.invoices = invoices;
        this.invoiceLines = invoiceLines;
        this.notes = notes;
        this.noteLines = noteLines;
        this.sequences = sequences;
        this.companyContext = companyContext;
        this.registeredCredits = registeredCredits;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long createFullDraft(
            Long tenantId,
            Long customerInvoiceId,
            String reason) {

        if (tenantId == null || customerInvoiceId == null) {
            throw new BusinessException(
                    "Tenant o fattura cliente mancanti.");
        }

        Long companyId = companyContext.requireCurrentCompanyId();

        if (reason == null || reason.isBlank()
                || reason.length() > 500) {
            throw new BusinessException(
                    "Motivazione obbligatoria (massimo 500 caratteri).");
        }

        var invoice = invoices
                .findByTenantIdAndCompanyIdAndId(
                        tenantId, companyId, customerInvoiceId)
                .orElseThrow(() -> new BusinessException(
                        "Fattura cliente non trovata nella società attiva."));

        if (!"ISSUED".equals(invoice.getStatus())) {
            throw new BusinessException(
                    "È possibile rettificare soltanto fatture ISSUED.");
        }

        var originals = invoiceLines
                .findByTenantIdAndInvoiceIdOrderByLineNoAsc(
                        tenantId, invoice.getId());

        if (originals.isEmpty()) {
            throw new BusinessException(
                    "La fattura non contiene righe rettificabili.");
        }

        List<CreditNoteLine> prepared = new ArrayList<>();

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;

        for (var original : originals) {

            BigDecimal quantity = original.getQuantity();
            BigDecimal lineTotal = original.getLineTotal();
            BigDecimal lineTax = original.getTaxAmount();

            if (quantity == null
                    || quantity.signum() <= 0
                    || quantity.scale() > 3
                    || lineTotal == null
                    || lineTax == null
                    || lineTotal.signum() < 0
                    || lineTax.signum() < 0
                    || lineTax.compareTo(lineTotal) > 0
                    || original.getLineNo() == null
                    || original.getDescription() == null
                    || original.getDescription().isBlank()
                    || original.getTaxPct() == null) {
                throw new BusinessException(
                        "Riga fattura cliente non valida.");
            }

            BigDecimal lineSubtotal =
                    lineTotal.subtract(lineTax);

            CreditNoteLine line = new CreditNoteLine();

            line.setTenantId(tenantId);
            line.setCompanyId(companyId);
            line.setOriginalInvoiceLineId(original.getId());
            line.setLineNo(original.getLineNo());
            line.setItemId(original.getItemId());
            line.setDescription(original.getDescription());
            line.setQuantity(quantity);
            line.setUnitOfMeasure(original.getUnitOfMeasure());
            line.setUomId(original.getUomId());
            line.setUomCode(original.getUnitOfMeasure());
            line.setBaseUomCode(original.getBaseUomCode());
            line.setConversionFactor(original.getConversionFactor());
            line.setUnitPrice(original.getUnitPrice());
            line.setDiscountPct(original.getDiscountPct());
            line.setTaxPct(original.getTaxPct());

            line.setLineSubtotal(lineSubtotal);
            line.setTaxAmount(lineTax);
            line.setLineTotal(lineTotal);

            prepared.add(line);

            subtotal = subtotal.add(lineSubtotal);
            tax = tax.add(lineTax);
            total = total.add(lineTotal);
        }

        if (invoice.getSubtotalAmount() == null
                || invoice.getTaxAmount() == null
                || invoice.getTotalAmount() == null
                || subtotal.compareTo(
                invoice.getSubtotalAmount()) != 0
                || tax.compareTo(
                invoice.getTaxAmount()) != 0
                || total.compareTo(
                invoice.getTotalAmount()) != 0
                || subtotal.add(tax).compareTo(total) != 0
                || total.signum() <= 0) {
            throw new BusinessException(
                    "Totali fattura cliente non coerenti con le righe.");
        }

        CreditNote note = new CreditNote();

        note.setTenantId(tenantId);
        note.setCompanyId(companyId);
        note.setCreditNoteType("CUSTOMER");
        note.setCreditNoteNumber(
                sequences.nextCustomerCreditNoteNumber(
                        tenantId, companyId));
        note.setCreditNoteDate(LocalDate.now());
        note.setOriginalInvoiceId(invoice.getId());
        note.setPartyId(invoice.getCustomerId());
        note.setStatus("DRAFT");
        note.setCurrencyCode(invoice.getCurrencyCode());
        note.setReason(reason.trim());
        note.setSubtotalAmount(subtotal);
        note.setTaxAmount(tax);
        note.setTotalAmount(total);

        CreditNote saved = notes.save(note);

        for (CreditNoteLine line : prepared) {
            line.setCreditNoteId(saved.getId());
        }

        noteLines.saveAll(prepared);

        return saved.getId();
    }

    // GESTIVA V2 - PASSO 5.13.2394U
    // Creazione nota di credito cliente parziale.
    // Nessuna contabilizzazione o modifica finanziaria.

    @Transactional(rollbackFor = Exception.class)
    public Long createPartialDraft(
            Long tenantId,
            Long customerInvoiceId,
            Map<Long, BigDecimal> quantitiesByLineId,
            String reason) {

        if (tenantId == null || customerInvoiceId == null) {
            throw new BusinessException(
                    "Tenant o fattura cliente mancanti.");
        }

        if (quantitiesByLineId == null
                || quantitiesByLineId.isEmpty()) {
            throw new BusinessException(
                    "Selezionare almeno una riga da rettificare.");
        }

        if (reason == null || reason.isBlank()
                || reason.length() > 500) {
            throw new BusinessException(
                    "Motivazione obbligatoria (massimo 500 caratteri).");
        }

        Long companyId = companyContext.requireCurrentCompanyId();

        var invoice = invoices
                .findByTenantIdAndCompanyIdAndId(
                        tenantId, companyId, customerInvoiceId)
                .orElseThrow(() -> new BusinessException(
                        "Fattura cliente non trovata nella società attiva."));

        if (!"ISSUED".equals(invoice.getStatus())) {
            throw new BusinessException(
                    "È possibile rettificare solo fatture ISSUED.");
        }

        var originals = invoiceLines
                .findByTenantIdAndInvoiceIdOrderByLineNoAsc(
                        tenantId, invoice.getId());

        if (originals.isEmpty()) {
            throw new BusinessException(
                    "Fattura cliente senza righe rettificabili.");
        }

        // Acquisisce il lock sulla fattura e legge
        // le rettifiche REGISTERED nella stessa transazione.
        var previousCredits = registeredCredits
                .loadRegisteredCredits(
                        tenantId, companyId, customerInvoiceId);

        Set<Long> foundIds = new HashSet<>();
        List<CreditNoteLine> prepared = new ArrayList<>();

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;

        for (var original : originals) {

            Long originalId = original.getId();

            if (originalId == null
                    || !tenantId.equals(original.getTenantId())
                    || !customerInvoiceId.equals(original.getInvoiceId())
                    || !foundIds.add(originalId)) {
                throw new BusinessException(
                        "Righe originali non valide o duplicate.");
            }

            if (!quantitiesByLineId.containsKey(originalId)) {
                continue;
            }

            BigDecimal requested =
                    quantitiesByLineId.get(originalId);

            var previous = previousCredits.getOrDefault(
                    originalId,
                    new CustomerCreditNoteCumulativeCalculator.PreviousCredits(
                            BigDecimal.ZERO,
                            BigDecimal.ZERO,
                            BigDecimal.ZERO));

            var amounts =
                    CustomerCreditNoteCumulativeCalculator.calculate(
                            original, previous, requested);

            if (original.getLineNo() == null
                    || original.getDescription() == null
                    || original.getDescription().isBlank()
                    || original.getTaxPct() == null
                    || original.getTaxPct().signum() < 0
                    || original.getLineTotal() == null
                    || original.getTaxAmount() == null
                    || original.getLineTotal().scale() > 2
                    || original.getTaxAmount().scale() > 2) {
                throw new BusinessException(
                        "Snapshot commerciale originario incompleto.");
            }

            CreditNoteLine line = new CreditNoteLine();

            line.setTenantId(tenantId);
            line.setCompanyId(companyId);
            line.setOriginalInvoiceLineId(originalId);
            line.setLineNo(original.getLineNo());
            line.setItemId(original.getItemId());
            line.setDescription(original.getDescription());

            line.setQuantity(amounts.quantity());
            line.setUnitOfMeasure(original.getUnitOfMeasure());
            line.setUomId(original.getUomId());
            line.setUomCode(original.getUnitOfMeasure());
            line.setBaseUomCode(original.getBaseUomCode());
            line.setConversionFactor(original.getConversionFactor());

            line.setUnitPrice(original.getUnitPrice());
            line.setDiscountPct(original.getDiscountPct());
            line.setTaxPct(original.getTaxPct());

            line.setLineSubtotal(amounts.subtotal());
            line.setTaxAmount(amounts.tax());
            line.setLineTotal(amounts.total());

            prepared.add(line);

            subtotal = subtotal.add(amounts.subtotal());
            tax = tax.add(amounts.tax());
            total = total.add(amounts.total());
        }

        // Esclude identificativi estranei alla fattura.
        if (prepared.size() != quantitiesByLineId.size()) {
            throw new BusinessException(
                    "Una o più righe richieste non appartengono "
                            + "alla fattura cliente.");
        }

        if (total.signum() <= 0
                || subtotal.signum() < 0
                || tax.signum() < 0
                || subtotal.add(tax).compareTo(total) != 0) {
            throw new BusinessException(
                    "Totali della nota cliente parziale non validi.");
        }

        // Numerazione solo dopo tutte le validazioni.
        CreditNote note = new CreditNote();

        note.setTenantId(tenantId);
        note.setCompanyId(companyId);
        note.setCreditNoteType("CUSTOMER");
        note.setCreditNoteNumber(
                sequences.nextCustomerCreditNoteNumber(
                        tenantId, companyId));
        note.setCreditNoteDate(LocalDate.now());
        note.setOriginalInvoiceId(invoice.getId());
        note.setPartyId(invoice.getCustomerId());
        note.setStatus("DRAFT");
        note.setCurrencyCode(invoice.getCurrencyCode());
        note.setReason(reason.trim());
        note.setSubtotalAmount(subtotal);
        note.setTaxAmount(tax);
        note.setTotalAmount(total);

        CreditNote saved = notes.save(note);

        for (CreditNoteLine line : prepared) {
            line.setCreditNoteId(saved.getId());
        }

        noteLines.saveAll(prepared);

        return saved.getId();
    }

}
