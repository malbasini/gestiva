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
public class SupplierCreditNoteFinancialPositionService {

    private static final BigDecimal ZERO = new BigDecimal("0.00");

    private final CompanyContext companyContext;
    private final CreditNoteRepository creditNotes;
    private final CreditNoteFinancialPositionRepository positions;

    public SupplierCreditNoteFinancialPositionService(
            CompanyContext companyContext,
            CreditNoteRepository creditNotes,
            CreditNoteFinancialPositionRepository positions) {

        this.companyContext = companyContext;
        this.creditNotes = creditNotes;
        this.positions = positions;
    }

    /**
     * Crea la posizione finanziaria iniziale.
     *
     * Invocare esclusivamente dal coordinatore atomico
     * della registrazione, dopo la contabilizzazione.
     *
     * La transazione deve essere già attiva.
     * Il coordinatore deve detenere il lock della
     * fattura originale e della nota.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public Long createForRegisteredSupplierCreditNote(
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

        var note = creditNotes
                .findByTenantIdAndCompanyIdAndId(
                        tenantId, companyId, creditNoteId)
                .orElseThrow(() -> new BusinessException(
                        "Nota di credito non trovata nella società attiva."));

        if (!"SUPPLIER".equals(note.getCreditNoteType())
                || !"REGISTERED".equals(note.getStatus())) {
            throw new BusinessException(
                    "La posizione richiede una nota fornitore registrata.");
        }

        if (note.getCurrencyCode() == null
                || note.getCurrencyCode().length() != 3) {
            throw new BusinessException(
                    "Valuta della nota non valida.");
        }

        BigDecimal amount = note.getTotalAmount();

        if (amount == null
                || amount.scale() > 2
                || amount.signum() <= 0) {
            throw new BusinessException(
                    "Importo della nota non valido.");
        }

        if (positions.existsByTenantIdAndCompanyIdAndCreditNoteId(
                tenantId, companyId, creditNoteId)) {
            throw new BusinessException(
                    "Posizione finanziaria della nota già esistente.");
        }

        CreditNoteFinancialPosition position =
                new CreditNoteFinancialPosition();

        position.setTenantId(tenantId);
        position.setCompanyId(companyId);
        position.setCreditNoteId(creditNoteId);
        position.setPositionType("SUPPLIER");
        position.setCurrencyCode(note.getCurrencyCode());
        position.setOriginalAmount(amount.setScale(2));
        position.setCompensatedAmount(ZERO);
        position.setRefundedAmount(ZERO);
        position.setAvailableAmount(amount.setScale(2));
        position.setStatus("OPEN");

        CreditNoteFinancialPosition saved = positions.save(position);

        if (saved == null || saved.getId() == null) {
            throw new BusinessException(
                    "Posizione finanziaria non salvata correttamente.");
        }

        return saved.getId();
    }
}
