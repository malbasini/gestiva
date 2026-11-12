
package com.gestiva.creditnote.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.creditnote.repository.CreditNoteRepository;
import com.gestiva.security.company.context.CompanyContext;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerCreditNoteRegistrationService {

    private final CompanyContext companyContext;
    private final CreditNoteRepository notes;
    private final CustomerCreditNoteRegistrationPreparationService preparation;
    private final CustomerCreditNoteAccountingService accounting;
    private final CustomerCreditNoteFinancialPositionService financialPositions;

    public CustomerCreditNoteRegistrationService(
            CompanyContext companyContext,
            CreditNoteRepository notes,
            CustomerCreditNoteRegistrationPreparationService preparation,
            CustomerCreditNoteAccountingService accounting,
            CustomerCreditNoteFinancialPositionService financialPositions) {

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
     * GESTIVA V2 - PASSO 5.13.2395L
     *
     * Registrazione atomica di una nota di credito CUSTOMER.
     *
     * 1. Validazione con lock della fattura e della nota.
     * 2. DRAFT -> REGISTERED.
     * 3. Scrittura contabile POSTED.
     * 4. Posizione finanziaria OPEN.
     *
     * Tutte le operazioni partecipano alla stessa transazione.
     *
     * Non esporre a controller o UI prima dei test
     * transazionali reali.
     */
    @Transactional(
            propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class)
    public RegistrationResult register(
            Long tenantId,
            Long creditNoteId) {

        if (tenantId == null || creditNoteId == null) {
            throw new BusinessException(
                    "Tenant o nota di credito cliente mancanti.");
        }

        Long companyId =
                companyContext.requireCurrentCompanyId();

        // Acquisisce i lock e valida nuovamente
        // quantità, importi e rettifiche precedenti.
        // Propagation.MANDATORY garantisce che
        // preparation partecipi alla transazione corrente.
        preparation.validateUnderLock(
                tenantId, creditNoteId);

        // La nota viene riletta nella stessa transazione
        // e nello stesso persistence context.
        // Il lock acquisito da preparation resta attivo.
        var note = notes
                .findByTenantIdAndCompanyIdAndId(
                        tenantId, companyId, creditNoteId)
                .orElseThrow(() -> new BusinessException(
                        "Nota cliente non disponibile "
                                + "dopo la validazione."));

        if (!"CUSTOMER".equals(note.getCreditNoteType())
                || !"DRAFT".equals(note.getStatus())) {
            throw new BusinessException(
                    "Nota cliente non più registrabile.");
        }

        // Il documento viene portato a REGISTERED
        // senza effettuare ancora il commit.
        note.setStatus("REGISTERED");

        var saved = notes.saveAndFlush(note);

        if (saved == null
                || saved.getId() == null
                || !creditNoteId.equals(saved.getId())) {
            throw new BusinessException(
                    "Registrazione della nota cliente non riuscita.");
        }

        // Scrittura contabile di storno.
        Long journalEntryId = accounting.postRegisteredNote(
                tenantId, creditNoteId);

        if (journalEntryId == null) {
            throw new BusinessException(
                    "Contabilizzazione della nota cliente non riuscita.");
        }

        // Posizione finanziaria a favore del cliente.
        Long financialPositionId =
                financialPositions
                        .createForRegisteredCustomerCreditNote(
                                tenantId, creditNoteId);

        if (financialPositionId == null) {
            throw new BusinessException(
                    "Posizione finanziaria cliente non creata.");
        }

        return new RegistrationResult(
                creditNoteId,
                journalEntryId,
                financialPositionId);
    }
}
