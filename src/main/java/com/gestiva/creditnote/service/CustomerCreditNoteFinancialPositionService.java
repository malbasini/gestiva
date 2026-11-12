
package com.gestiva.creditnote.service;

import com.gestiva.common.exception.BusinessException;
import com.gestiva.creditnote.entity.CreditNoteFinancialPosition;
import com.gestiva.creditnote.repository.CreditNoteFinancialPositionRepository;
import com.gestiva.creditnote.repository.CreditNoteRepository;
import com.gestiva.security.company.context.CompanyContext;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class CustomerCreditNoteFinancialPositionService {

    private static final BigDecimal ZERO =
            new BigDecimal("0.00");

    private final CompanyContext companyContext;
    private final CreditNoteRepository creditNotes;
    private final CreditNoteFinancialPositionRepository positions;

    public CustomerCreditNoteFinancialPositionService(
            CompanyContext companyContext,
            CreditNoteRepository creditNotes,
            CreditNoteFinancialPositionRepository positions) {

        this.companyContext = companyContext;
        this.creditNotes = creditNotes;
        this.positions = positions;
    }

    /**
     * GESTIVA V2 - PASSO 5.13.2395I
     *
     * Crea la posizione finanziaria iniziale
     * di una nota di credito CUSTOMER registrata.
     *
     * Deve essere richiamato esclusivamente dal
     * coordinatore atomico di registrazione.
     *
     * Il coordinatore deve già detenere il lock
     * della fattura originale e della nota.
     *
     * Non crea pagamenti, compensazioni
     * o scritture contabili.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public Long createForRegisteredCustomerCreditNote(
            Long tenantId,
            Long creditNoteId) {

        if (tenantId == null || creditNoteId == null) {
            throw new BusinessException(
                    "Tenant o nota di credito cliente mancanti.");
        }

        Long companyId =
                companyContext.requireCurrentCompanyId();

        var note = creditNotes
                .findByTenantIdAndCompanyIdAndId(
                        tenantId, companyId, creditNoteId)
                .orElseThrow(() -> new BusinessException(
                        "Nota cliente non trovata nella società attiva."));

        if (!"CUSTOMER".equals(note.getCreditNoteType())
                || !"REGISTERED".equals(note.getStatus())) {
            throw new BusinessException(
                    "La posizione finanziaria richiede "
                            + "una nota cliente REGISTERED.");
        }

        if (note.getOriginalInvoiceId() == null
                || note.getPartyId() == null
                || note.getCurrencyCode() == null
                || note.getCurrencyCode().length() != 3) {
            throw new BusinessException(
                    "Dati della nota cliente incompleti.");
        }

        BigDecimal amount = note.getTotalAmount();

        if (amount == null
                || amount.scale() > 2
                || amount.signum() <= 0) {
            throw new BusinessException(
                    "Importo della nota cliente non valido.");
        }

        if (positions
                .existsByTenantIdAndCompanyIdAndCreditNoteId(
                        tenantId, companyId, creditNoteId)) {
            throw new BusinessException(
                    "Posizione finanziaria cliente già esistente.");
        }

        CreditNoteFinancialPosition position =
                new CreditNoteFinancialPosition();

        position.setTenantId(tenantId);
        position.setCompanyId(companyId);
        position.setCreditNoteId(creditNoteId);
        position.setPositionType("CUSTOMER");
        position.setCurrencyCode(note.getCurrencyCode());

        position.setOriginalAmount(amount.setScale(2));
        position.setCompensatedAmount(ZERO);
        position.setRefundedAmount(ZERO);
        position.setAvailableAmount(amount.setScale(2));
        position.setStatus("OPEN");

        CreditNoteFinancialPosition saved =
                positions.save(position);

        if (saved == null || saved.getId() == null) {
            throw new BusinessException(
                    "Posizione finanziaria cliente "
                            + "non salvata correttamente.");
        }

        return saved.getId();
    }
}
