package com.gestiva.creditnote.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.creditnote.entity.CreditNote;
import com.gestiva.creditnote.entity.CreditNoteLine;
import com.gestiva.creditnote.repository.CreditNoteLineRepository;
import com.gestiva.creditnote.repository.CreditNoteRepository;
import com.gestiva.purchasing.invoice.repository.SupplierInvoiceLineRepository;
import com.gestiva.purchasing.invoice.repository.SupplierInvoiceRepository;
import com.gestiva.security.company.context.CompanyContext;
import com.gestiva.settings.sequence.service.DocumentSequenceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

@Service
public class SupplierCreditNoteDraftService {

    private static final String DOCUMENT_TYPE = "SUPPLIER_CREDIT_NOTE";

    private final SupplierInvoiceRepository invoiceRepository;
    private final SupplierInvoiceLineRepository invoiceLineRepository;
    private final CreditNoteRepository creditNoteRepository;
    private final CreditNoteLineRepository creditNoteLineRepository;
    private final DocumentSequenceService sequences;
    private final CompanyContext companyContext;
    private final SupplierCreditNoteRegisteredCreditsService registeredCredits;

    public SupplierCreditNoteDraftService(
            SupplierInvoiceRepository invoiceRepository,
            SupplierInvoiceLineRepository invoiceLineRepository,
            CreditNoteRepository creditNoteRepository,
            CreditNoteLineRepository creditNoteLineRepository,
            DocumentSequenceService sequences,
            CompanyContext companyContext,
            SupplierCreditNoteRegisteredCreditsService registeredCredits) {

        this.invoiceRepository = invoiceRepository;
        this.invoiceLineRepository = invoiceLineRepository;
        this.creditNoteRepository = creditNoteRepository;
        this.creditNoteLineRepository = creditNoteLineRepository;
        this.sequences = sequences;
        this.companyContext = companyContext;
        this.registeredCredits = registeredCredits;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long createFullDraft(
            Long tenantId,
            Long supplierInvoiceId,
            String reason) {

        if (tenantId == null || supplierInvoiceId == null) {
            throw new BusinessException("Tenant o fattura non validi.");
        }

        Long companyId = companyContext.resolveCurrentCompanyId();

        if (companyId == null) {
            throw new BusinessException(
                    "Nessuna società attiva selezionata.");
        }

        if (reason == null || reason.isBlank()
                || reason.length() > 500) {
            throw new BusinessException(
                    "Motivazione obbligatoria (massimo 500 caratteri).");
        }

        var invoice = invoiceRepository
                .findByTenantIdAndCompanyIdAndId(
                        tenantId, companyId, supplierInvoiceId)
                .orElseThrow(() -> new BusinessException(
                        "Fattura fornitore non trovata nella società attiva."));

        if (!"REGISTERED".equals(invoice.getStatus())) {
            throw new BusinessException(
                    "È possibile rettificare soltanto fatture registrate.");
        }

        var originalLines = invoiceLineRepository
                .findByTenantIdAndSupplierInvoiceIdOrderByLineNoAsc(
                        tenantId, invoice.getId());

        if (originalLines.isEmpty()) {
            throw new BusinessException(
                    "La fattura non contiene righe rettificabili.");
        }

        // Preparazione in memoria: nessuna scrittura prima
        // della validazione delle righe originali.
        List<CreditNoteLine> prepared = new ArrayList<>();

        for (var original : originalLines) {

            if (original.getQuantity() == null
                    || original.getQuantity().signum() <= 0
                    || original.getLineSubtotal() == null
                    || original.getTaxAmount() == null
                    || original.getLineTotal() == null) {
                throw new BusinessException(
                        "Riga fattura con quantità o importi non validi.");
            }

            CreditNoteLine line = new CreditNoteLine();
            line.setTenantId(tenantId);
            line.setCompanyId(companyId);
            line.setOriginalInvoiceLineId(original.getId());
            line.setLineNo(original.getLineNo());
            line.setItemId(original.getItemId());
            line.setDescription(original.getDescription());
            line.setQuantity(original.getQuantity());
            line.setUnitOfMeasure(original.getUomCode());
            line.setUomId(original.getUomId());
            line.setUomCode(original.getUomCode());
            line.setBaseUomCode(original.getBaseUomCode());
            line.setConversionFactor(original.getConversionFactor());
            line.setUnitPrice(original.getUnitPrice());
            line.setDiscountPct(original.getDiscountPct());
            line.setTaxPct(original.getTaxPct());
            line.setLineSubtotal(original.getLineSubtotal());
            line.setTaxAmount(original.getTaxAmount());
            line.setLineTotal(original.getLineTotal());

            prepared.add(line);
        }

        // La bozza totale deve riconciliare esattamente
        // imponibile, IVA e totale della fattura originaria.
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;

        for (CreditNoteLine line : prepared) {
            if (line.getLineSubtotal().signum() < 0
                    || line.getTaxAmount().signum() < 0
                    || line.getLineTotal().signum() < 0
                    || line.getLineSubtotal()
                            .add(line.getTaxAmount())
                            .compareTo(line.getLineTotal()) != 0) {
                throw new BusinessException(
                        "Importi della riga fattura non coerenti.");
            }

            subtotal = subtotal.add(line.getLineSubtotal());
            tax = tax.add(line.getTaxAmount());
            total = total.add(line.getLineTotal());
        }

        if (invoice.getSubtotalAmount() == null
                || invoice.getTaxAmount() == null
                || invoice.getTotalAmount() == null
                || subtotal.compareTo(invoice.getSubtotalAmount()) != 0
                || tax.compareTo(invoice.getTaxAmount()) != 0
                || total.compareTo(invoice.getTotalAmount()) != 0
                || subtotal.add(tax).compareTo(total) != 0) {
            throw new BusinessException(
                    "I totali delle righe non coincidono con la fattura.");
        }

        CreditNote note = new CreditNote();
        note.setTenantId(tenantId);
        note.setCompanyId(companyId);
        note.setCreditNoteType("SUPPLIER");
        note.setCreditNoteNumber(
                sequences.nextNumber(tenantId, companyId, DOCUMENT_TYPE));
        note.setCreditNoteDate(java.time.LocalDate.now());
        note.setOriginalInvoiceId(invoice.getId());
        note.setPartyId(invoice.getSupplierId());
        note.setStatus("DRAFT");
        note.setCurrencyCode(invoice.getCurrencyCode());
        note.setReason(reason.trim());
        note.setSubtotalAmount(invoice.getSubtotalAmount());
        note.setTaxAmount(invoice.getTaxAmount());
        note.setTotalAmount(invoice.getTotalAmount());

        CreditNote saved = creditNoteRepository.save(note);

        for (CreditNoteLine line : prepared) {
            line.setCreditNoteId(saved.getId());
        }

        creditNoteLineRepository.saveAll(prepared);

        return saved.getId();
    }

    /**
     * Prepara una bozza di rettifica parziale per quantità.
     *
     * Controlla preventivamente gli storni REGISTERED sotto lock.
     * La registrazione ripete la validazione nella propria
     * transazione. Le altre bozze DRAFT non consumano residuo.
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createPartialDraft(
            Long tenantId,
            Long supplierInvoiceId,
            Map<Long, BigDecimal> quantitiesByLineId,
            String reason) {

        if (tenantId == null || supplierInvoiceId == null) {
            throw new BusinessException(
                    "Tenant o fattura non validi.");
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

        Long companyId = companyContext.resolveCurrentCompanyId();

        if (companyId == null) {
            throw new BusinessException(
                    "Nessuna società attiva selezionata.");
        }

        var invoice = invoiceRepository
                .findByTenantIdAndCompanyIdAndId(
                        tenantId, companyId, supplierInvoiceId)
                .orElseThrow(() -> new BusinessException(
                        "Fattura non trovata nella società attiva."));

        if (!"REGISTERED".equals(invoice.getStatus())) {
            throw new BusinessException(
                    "È possibile rettificare solo fatture registrate.");
        }

        var originals = invoiceLineRepository
                .findByTenantIdAndSupplierInvoiceIdOrderByLineNoAsc(
                        tenantId, supplierInvoiceId);

        if (originals.isEmpty()) {
            throw new BusinessException(
                    "Fattura senza righe rettificabili.");
        }

        // Prevalidazione cumulativa prima del numeratore.
        // Il lock è mantenuto dalla transazione corrente.
        var previousCredits = registeredCredits.loadRegisteredCredits(
                tenantId, companyId, supplierInvoiceId);

        Set<Long> foundIds = new HashSet<>();
        List<CreditNoteLine> prepared = new ArrayList<>();

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;

        for (var original : originals) {
            Long originalId = original.getId();

            if (originalId == null || !foundIds.add(originalId)) {
                throw new BusinessException(
                        "Identificativi righe originali non validi.");
            }

            if (!quantitiesByLineId.containsKey(originalId)) {
                continue;
            }

            BigDecimal requested = quantitiesByLineId.get(originalId);

            var previous = previousCredits.getOrDefault(
                    originalId,
                    new SupplierCreditNoteCumulativeCalculator.PreviousCredits(
                            BigDecimal.ZERO,
                            BigDecimal.ZERO,
                            BigDecimal.ZERO));

            var amounts =
                    SupplierCreditNoteCumulativeCalculator.calculate(
                            original, previous, requested);

            if (original.getLineNo() == null
                    || original.getDescription() == null
                    || original.getTaxPct() == null) {
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
            line.setUnitOfMeasure(original.getUomCode());
            line.setUomId(original.getUomId());
            line.setUomCode(original.getUomCode());
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

        if (prepared.size() != quantitiesByLineId.size()) {
            throw new BusinessException(
                    "Una o più righe richieste non appartengono "
                    + "alla fattura originale.");
        }

        if (subtotal.signum() < 0
                || tax.signum() < 0
                || total.signum() <= 0
                || subtotal.add(tax).compareTo(total) != 0) {
            throw new BusinessException(
                    "Importi della bozza parziale non validi.");
        }

        // Tutte le validazioni precedono il consumo
        // del numeratore e il salvataggio dei documenti.
        CreditNote note = new CreditNote();
        note.setTenantId(tenantId);
        note.setCompanyId(companyId);
        note.setCreditNoteType("SUPPLIER");
        note.setCreditNoteNumber(
                sequences.nextNumber(
                        tenantId, companyId, DOCUMENT_TYPE));
        note.setCreditNoteDate(java.time.LocalDate.now());
        note.setOriginalInvoiceId(invoice.getId());
        note.setPartyId(invoice.getSupplierId());
        note.setStatus("DRAFT");
        note.setCurrencyCode(invoice.getCurrencyCode());
        note.setReason(reason.trim());
        note.setSubtotalAmount(subtotal);
        note.setTaxAmount(tax);
        note.setTotalAmount(total);

        CreditNote saved = creditNoteRepository.save(note);

        for (CreditNoteLine line : prepared) {
            line.setCreditNoteId(saved.getId());
        }

        creditNoteLineRepository.saveAll(prepared);

        return saved.getId();
    }

}
