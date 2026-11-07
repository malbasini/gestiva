package com.gestiva.creditnote.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.creditnote.repository.CreditNoteRepository;
import com.gestiva.security.company.context.CompanyContext;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SupplierCreditNoteRegistrationService {

    private final CompanyContext companyContext;
    private final CreditNoteRepository notes;
    private final SupplierCreditNoteRegistrationPreparationService preparation;
    private final SupplierCreditNoteAccountingService accounting;
    private final SupplierCreditNoteFinancialPositionService financialPositions;

    public SupplierCreditNoteRegistrationService(
            CompanyContext companyContext,
            CreditNoteRepository notes,
            SupplierCreditNoteRegistrationPreparationService preparation,
            SupplierCreditNoteAccountingService accounting,
            SupplierCreditNoteFinancialPositionService financialPositions) {

        this.companyContext = companyContext;
        this.notes = notes;
        this.preparation = preparation;
        this.accounting = accounting;
        this.financialPositions = financialPositions;
    }

    public record RegistrationResult(
            Long creditNoteId,
            Long journalEntryId,
            Long financialPositionId) {
    }

    /**
     * Registra una nota di credito fornitore in modo atomico.
     *
     * La registrazione comprende:
     * 1. Validazione e lock della fattura e della bozza.
     * 2. Cambio stato DRAFT -> REGISTERED.
     * 3. Scrittura contabile di storno POSTED.
     * 4. Posizione finanziaria OPEN.
     *
     * Tutti i servizi partecipano alla stessa transazione.
     *
     * NON esporre a controller o UI prima dei test
     * transazionali reali e dell'applicazione controllata
     * della migrazione V9.
     */
    @Transactional(
            propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class)
    public RegistrationResult register(
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

        // La validazione acquista prima il lock sulla
        // fattura originale, poi quello sulla nota.
        //
        // Il metodo REQUIRED partecipa alla transazione
        // corrente, senza rilasciare anticipatamente i lock.
        preparation.validateUnderLock(
                tenantId, creditNoteId);

        // Stessa transazione e stesso persistence context.
        // Il lock della nota resta detenuto.
        var note = notes
                .findByTenantIdAndCompanyIdAndId(
                        tenantId, companyId, creditNoteId)
                .orElseThrow(() -> new BusinessException(
                        "Nota non disponibile dopo la validazione."));

        // Protezione aggiuntiva prima della scrittura.
        if (!"SUPPLIER".equals(note.getCreditNoteType())
                || !"DRAFT".equals(note.getStatus())) {
            throw new BusinessException(
                    "Nota non più registrabile.");
        }

        note.setStatus("REGISTERED");

        // Forza il controllo dei vincoli SQL e sincronizza
        // lo stato prima dei servizi successivi.
        //
        // NON esegue commit: il rollback rimane possibile.
        var saved = notes.saveAndFlush(note);

        if (saved == null || saved.getId() == null
                || !creditNoteId.equals(saved.getId())) {
            throw new BusinessException(
                    "Registrazione della nota non riuscita.");
        }

        // Scrittura contabile di storno.
        // Il servizio richiede una transazione attiva.
        Long journalEntryId = accounting.postRegisteredNote(
                tenantId, creditNoteId);

        if (journalEntryId == null) {
            throw new BusinessException(
                    "Contabilizzazione della nota non riuscita.");
        }

        // Posizione finanziaria verso il fornitore.
        // Anche questo servizio richiede la transazione.
        Long financialPositionId =
                financialPositions
                    .createForRegisteredSupplierCreditNote(
                            tenantId, creditNoteId);

        if (financialPositionId == null) {
            throw new BusinessException(
                    "Posizione finanziaria non creata.");
        }

        return new RegistrationResult(
                creditNoteId,
                journalEntryId,
                financialPositionId);
    }
}
