
package com.gestiva.creditnote.service;

import com.gestiva.billing.invoice.repository.InvoiceLineRepository;
import com.gestiva.billing.invoice.repository.InvoiceRepository;
import com.gestiva.common.exception.BusinessException;
import com.gestiva.creditnote.repository.CreditNoteLineRepository;
import com.gestiva.creditnote.repository.CreditNoteRepository;
import com.gestiva.security.company.context.CompanyContext;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class CustomerCreditNoteRegistrationPreparationService {

    private final CompanyContext companyContext;
    private final InvoiceRepository invoices;
    private final InvoiceLineRepository invoiceLines;
    private final CreditNoteRepository notes;
    private final CreditNoteLineRepository noteLines;
    private final CustomerCreditNoteRegisteredCreditsService registeredCredits;

    public CustomerCreditNoteRegistrationPreparationService(
            CompanyContext companyContext,
            InvoiceRepository invoices,
            InvoiceLineRepository invoiceLines,
            CreditNoteRepository notes,
            CreditNoteLineRepository noteLines,
            CustomerCreditNoteRegisteredCreditsService registeredCredits) {

        this.companyContext = companyContext;
        this.invoices = invoices;
        this.invoiceLines = invoiceLines;
        this.notes = notes;
        this.noteLines = noteLines;
        this.registeredCredits = registeredCredits;
    }

    /**
     * GESTIVA V2 - PASSO 5.13.2395A
     *
     * Validazione transazionale di una nota di credito
     * cliente prima della registrazione definitiva.
     *
     * Ordine dei lock:
     * 1. Fattura cliente originale
     * 2. Nota di credito DRAFT
     *
     * Non modifica lo stato dei documenti.
     * Non genera scritture contabili.
     * Non crea posizioni finanziarie.
     *
     * Deve essere invocato dal coordinatore di
     * registrazione nella stessa transazione.
     */
    @Transactional(
            propagation = Propagation.MANDATORY,
            rollbackFor = Exception.class)
    public void validateUnderLock(
            Long tenantId,
            Long creditNoteId) {

        if (tenantId == null || creditNoteId == null) {
            throw new BusinessException(
                    "Tenant o nota di credito cliente mancanti.");
        }

        Long companyId =
                companyContext.requireCurrentCompanyId();

        // Prima lettura senza lock: serve esclusivamente
        // a individuare la fattura originale.
        var initialNote = notes
                .findByTenantIdAndCompanyIdAndId(
                        tenantId, companyId, creditNoteId)
                .orElseThrow(() -> new BusinessException(
                        "Nota di credito cliente non trovata "
                                + "nella società attiva."));

        if (!"CUSTOMER".equals(initialNote.getCreditNoteType())
                || initialNote.getOriginalInvoiceId() == null) {
            throw new BusinessException(
                    "Documento non valido per note di credito clienti.");
        }

        Long originalInvoiceId =
                initialNote.getOriginalInvoiceId();

        // Lock principale: serializza le registrazioni
        // concorrenti riferite alla stessa fattura.
        var originalInvoice = invoices
                .findByTenantIdAndCompanyIdAndIdForUpdate(
                        tenantId, companyId, originalInvoiceId)
                .orElseThrow(() -> new BusinessException(
                        "Fattura cliente originale non trovata "
                                + "nella società attiva."));

        if (!"ISSUED".equals(originalInvoice.getStatus())) {
            throw new BusinessException(
                    "La fattura cliente originale deve essere ISSUED.");
        }

        // Il lock della nota viene acquisito soltanto
        // dopo quello della fattura.
        var draft = notes
                .findByTenantIdAndCompanyIdAndIdForUpdate(
                        tenantId, companyId, creditNoteId)
                .orElseThrow(() -> new BusinessException(
                        "Bozza cliente non più disponibile."));

        // Controlla nuovamente lo stato e il riferimento
        // dopo l'acquisizione dei lock.
        if (!"CUSTOMER".equals(draft.getCreditNoteType())
                || !"DRAFT".equals(draft.getStatus())
                || !Objects.equals(
                originalInvoiceId,
                draft.getOriginalInvoiceId())
                || !Objects.equals(
                originalInvoice.getCustomerId(),
                draft.getPartyId())
                || !Objects.equals(
                originalInvoice.getCurrencyCode(),
                draft.getCurrencyCode())) {

            throw new BusinessException(
                    "Bozza cliente modificata, incoerente "
                            + "o non più registrabile.");
        }

        // Ricostruisce le rettifiche registrate.
        // La chiamata partecipa alla stessa transazione.
        var previousCredits = registeredCredits
                .loadRegisteredCredits(
                        tenantId,
                        companyId,
                        originalInvoiceId);

        // Righe della fattura verificata per tenant/company.
        var originals = invoiceLines
                .findByTenantIdAndInvoiceIdOrderByLineNoAsc(
                        tenantId, originalInvoiceId);

        // Righe della nota della società attiva.
        var draftRows = noteLines
                .findByTenantIdAndCompanyIdAndCreditNoteIdOrderByLineNoAsc(
                        tenantId, companyId, creditNoteId);

        // Validazione definitiva dei dati commerciali,
        // quantità, IVA e rettifiche cumulative.
        CustomerCreditNoteRegistrationValidator.validate(
                originalInvoice,
                draft,
                originals,
                draftRows,
                previousCredits);
    }
}

