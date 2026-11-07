package com.gestiva.creditnote.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.creditnote.repository.CreditNoteLineRepository;
import com.gestiva.creditnote.repository.CreditNoteRepository;
import com.gestiva.purchasing.invoice.repository.SupplierInvoiceLineRepository;
import com.gestiva.purchasing.invoice.repository.SupplierInvoiceRepository;
import com.gestiva.security.company.context.CompanyContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SupplierCreditNoteRegistrationPreparationService {

    private final CompanyContext companyContext;
    private final SupplierInvoiceRepository invoices;
    private final SupplierInvoiceLineRepository invoiceLines;
    private final CreditNoteRepository notes;
    private final CreditNoteLineRepository noteLines;
    private final SupplierCreditNoteRegisteredCreditsService registeredCredits;

    public SupplierCreditNoteRegistrationPreparationService(
            CompanyContext companyContext,
            SupplierInvoiceRepository invoices,
            SupplierInvoiceLineRepository invoiceLines,
            CreditNoteRepository notes,
            CreditNoteLineRepository noteLines,
            SupplierCreditNoteRegisteredCreditsService registeredCredits) {

        this.companyContext = companyContext;
        this.invoices = invoices;
        this.invoiceLines = invoiceLines;
        this.notes = notes;
        this.noteLines = noteLines;
        this.registeredCredits = registeredCredits;
    }

    /**
     * Validazione transazionale della bozza.
     *
     * Non registra la nota e non effettua scritture.
     * Il lock viene mantenuto fino alla conclusione
     * della transazione che esegue questo metodo.
     *
     * IMPORTANTE: una chiamata di prevalidazione conclusa
     * non autorizza una registrazione successiva.
     * La futura registrazione dovrà ripetere la verifica
     * nella propria transazione atomica.
     */
    @Transactional(
            propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class)
    public void validateUnderLock(
            Long tenantId,
            Long creditNoteId) {

        if (tenantId == null || creditNoteId == null) {
            throw new BusinessException(
                    "Tenant o nota di credito mancanti.");
        }

        Long companyId = companyContext.resolveCurrentCompanyId();

        if (companyId == null) {
            throw new BusinessException(
                    "Nessuna società attiva selezionata.");
        }

        // Prima lettura per individuare la fattura.
        // Non costituisce ancora una validazione definitiva.
        var initialNote = notes
                .findByTenantIdAndCompanyIdAndId(
                        tenantId, companyId, creditNoteId)
                .orElseThrow(() -> new BusinessException(
                        "Nota di credito non trovata nella società attiva."));

        if (!"SUPPLIER".equals(initialNote.getCreditNoteType())
                || initialNote.getOriginalInvoiceId() == null) {
            throw new BusinessException(
                    "Documento non valido per note di credito fornitori.");
        }

        Long originalInvoiceId = initialNote.getOriginalInvoiceId();

        // Lock principale: tutte le note relative alla stessa
        // fattura devono essere serializzate su questa riga.
        var originalInvoice = invoices
                .findByTenantIdAndCompanyIdAndIdForUpdate(
                        tenantId, companyId, originalInvoiceId)
                .orElseThrow(() -> new BusinessException(
                        "Fattura originale non trovata nella società attiva."));

        // Lock della bozza dopo quello della fattura.
        var draft = notes
                .findByTenantIdAndCompanyIdAndIdForUpdate(
                        tenantId, companyId, creditNoteId)
                .orElseThrow(() -> new BusinessException(
                        "Bozza non più disponibile."));

        if (!"SUPPLIER".equals(draft.getCreditNoteType())
                || !"DRAFT".equals(draft.getStatus())
                || !originalInvoiceId.equals(draft.getOriginalInvoiceId())) {
            throw new BusinessException(
                    "Bozza modificata o non più registrabile.");
        }

        // Richiama la lettura protetta nella transazione corrente.
        // Il secondo lock sulla stessa fattura è riacquisito
        // dalla medesima transazione, non da una transazione separata.
        var previous = registeredCredits.loadRegisteredCredits(
                tenantId, companyId, originalInvoiceId);

        var originals = invoiceLines
                .findByTenantIdAndSupplierInvoiceIdOrderByLineNoAsc(
                        tenantId, originalInvoiceId);

        var draftRows = noteLines
                .findByTenantIdAndCompanyIdAndCreditNoteIdOrderByLineNoAsc(
                        tenantId, companyId, creditNoteId);

        SupplierCreditNoteRegistrationValidator.validate(
                originalInvoice,
                draft,
                originals,
                draftRows,
                previous);
    }
}
