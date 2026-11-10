package com.gestiva.creditnote.service;

import com.gestiva.accounting.due.entity.PaymentDue;
import com.gestiva.accounting.due.repository.PaymentDueRepository;
import com.gestiva.common.exception.BusinessException;
import com.gestiva.creditnote.entity.CreditNoteFinancialPosition;
import com.gestiva.creditnote.entity.SupplierCreditNoteCompensation;
import com.gestiva.creditnote.repository.CreditNoteFinancialPositionRepository;
import com.gestiva.creditnote.repository.CreditNoteRepository;
import com.gestiva.creditnote.repository.SupplierCreditNoteCompensationRepository;
import com.gestiva.security.company.context.CompanyContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Service
public class SupplierCreditNoteCompensationService {

    private final CompanyContext companyContext;
    private final CreditNoteFinancialPositionRepository positions;
    private final CreditNoteRepository creditNotes;
    private final PaymentDueRepository dues;
    private final SupplierCreditNoteCompensationRepository compensations;

    public SupplierCreditNoteCompensationService(
            CompanyContext companyContext,
            CreditNoteFinancialPositionRepository positions,
            CreditNoteRepository creditNotes,
            PaymentDueRepository dues,
            SupplierCreditNoteCompensationRepository compensations) {

        this.companyContext = companyContext;
        this.positions = positions;
        this.creditNotes = creditNotes;
        this.dues = dues;
        this.compensations = compensations;
    }

    /**
     * Compensa una posizione di credito fornitore con una
     * scadenza passiva dello stesso fornitore e societa.
     *
     * Ordine dei lock: posizione finanziaria, poi scadenza.
     * Il chiamante deve utilizzare una operationKey stabile:
     * ripetere la stessa richiesta non deve utilizzare
     * nuovamente il credito.
     *
     * Non genera pagamenti bancari ne ulteriori scritture
     * contabili: fattura e nota sono gia contabilizzate.
     */
    @Transactional(rollbackFor = Exception.class)
    public Long compensate(
            Long tenantId,
            Long positionId,
            Long paymentDueId,
            BigDecimal amount,
            LocalDate compensationDate,
            String operationKey,
            String notes) {

        if (tenantId == null || positionId == null
                || paymentDueId == null) {
            throw new BusinessException(
                    "Identificativi della compensazione mancanti.");
        }

        if (compensationDate == null) {
            throw new BusinessException(
                    "Data della compensazione obbligatoria.");
        }

        if (operationKey == null || operationKey.isBlank()
                || operationKey.length() > 100) {
            throw new BusinessException(
                    "Identificativo operazione non valido.");
        }

        String key = operationKey.trim();

        if (notes != null && notes.length() > 1000) {
            throw new BusinessException(
                    "Note della compensazione troppo lunghe.");
        }

        BigDecimal value = requireMoney(amount);

        Long companyId = companyContext.requireCurrentCompanyId();

        // Il lock sulla posizione precede sempre quello
        // sulla scadenza. Restano entrambi fino al commit.
        CreditNoteFinancialPosition position = positions
                .findByTenantIdAndCompanyIdAndIdForUpdate(
                        tenantId, companyId, positionId)
                .orElseThrow(() -> new BusinessException(
                        "Posizione finanziaria non trovata nella societa."));

        PaymentDue due = dues
                .findByTenantIdAndCompanyIdAndIdForUpdate(
                        tenantId, companyId, paymentDueId)
                .orElseThrow(() -> new BusinessException(
                        "Scadenza non trovata nella societa."));

        var note = creditNotes
                .findByTenantIdAndCompanyIdAndId(
                        tenantId, companyId, position.getCreditNoteId())
                .orElseThrow(() -> new BusinessException(
                        "Nota di credito non trovata nella societa."));

        if (!"SUPPLIER".equals(note.getCreditNoteType())
                || !"REGISTERED".equals(note.getStatus())
                || !"SUPPLIER".equals(position.getPositionType())) {
            throw new BusinessException(
                    "Posizione non riferita a una nota fornitore registrata.");
        }

        if (!"PAYABLE".equals(due.getDirection())
                || !"SUPPLIER".equals(due.getPartyType())
                || !"SUPPLIER_INVOICE".equals(due.getReferenceType())) {
            throw new BusinessException(
                    "La scadenza non rappresenta una fattura fornitore.");
        }

        if (!Objects.equals(note.getPartyId(), due.getPartyId())) {
            throw new BusinessException(
                    "Nota e scadenza appartengono a fornitori diversi.");
        }

        if (!Objects.equals(note.getCurrencyCode(), due.getCurrencyCode())
                || !Objects.equals(
                        position.getCurrencyCode(), due.getCurrencyCode())) {
            throw new BusinessException(
                    "La valuta della nota non coincide con la scadenza.");
        }

        // Controllo di idempotenza dopo l'acquisizione dei lock.
        // Una chiave gia utilizzata con parametri differenti
        // non puo identificare una nuova operazione.
        var existing = compensations
                .findByTenantIdAndCompanyIdAndOperationKey(
                        tenantId, companyId, key);

        if (existing.isPresent()) {
            var previous = existing.get();
            if (!Objects.equals(
                        previous.getCreditNoteFinancialPositionId(),
                        positionId)
                    || !Objects.equals(
                        previous.getPaymentDueId(), paymentDueId)
                    || previous.getAmount() == null
                    || previous.getAmount().compareTo(value) != 0
                    || !Objects.equals(
                        previous.getCompensationDate(),
                        compensationDate)
                    || !Objects.equals(previous.getNotes(), notes)) {
                throw new BusinessException(
                        "Identificativo operazione gia usato "
                        + "con dati differenti.");
            }

            return previous.getId();
        }

        if (!"OPEN".equals(position.getStatus())
                && !"PARTIALLY_SETTLED".equals(position.getStatus())) {
            throw new BusinessException(
                    "Posizione finanziaria non compensabile.");
        }

        if (!"OPEN".equals(due.getStatus())
                && !"PARTIALLY_PAID".equals(due.getStatus())) {
            throw new BusinessException(
                    "Scadenza non disponibile per compensazione.");
        }

        BigDecimal creditOriginal = requireStoredMoney(
                position.getOriginalAmount());
        BigDecimal creditCompensated = requireStoredMoney(
                position.getCompensatedAmount());
        BigDecimal creditRefunded = requireStoredMoney(
                position.getRefundedAmount());
        BigDecimal creditAvailable = requireStoredMoney(
                position.getAvailableAmount());

        if (creditOriginal.signum() <= 0
                || creditCompensated.signum() < 0
                || creditRefunded.signum() < 0
                || creditAvailable.signum() <= 0
                || creditOriginal.compareTo(
                    creditCompensated.add(creditRefunded)
                        .add(creditAvailable)) != 0) {
            throw new BusinessException(
                    "Posizione finanziaria non quadrata.");
        }

        BigDecimal gross = requireStoredMoney(due.getGrossAmount());
        BigDecimal paid = requireStoredMoney(due.getPaidAmount());
        BigDecimal dueCompensated =
                requireStoredMoney(due.getCompensatedAmount());
        BigDecimal open = requireStoredMoney(due.getOpenAmount());

        if (gross.signum() <= 0 || paid.signum() < 0
                || dueCompensated.signum() < 0
                || open.signum() <= 0
                || gross.compareTo(
                    paid.add(dueCompensated).add(open)) != 0) {
            throw new BusinessException(
                    "Scadenza non quadrata o senza residuo.");
        }

        if (value.compareTo(creditAvailable) > 0
                || value.compareTo(open) > 0) {
            throw new BusinessException(
                    "Compensazione superiore al credito "
                    + "o al debito residuo.");
        }

        BigDecimal newCreditAvailable = creditAvailable.subtract(value);
        BigDecimal newDueOpen = open.subtract(value);

        position.setCompensatedAmount(
                creditCompensated.add(value));
        position.setAvailableAmount(newCreditAvailable);
        position.setStatus(
                newCreditAvailable.signum() == 0
                        ? "SETTLED"
                        : "PARTIALLY_SETTLED");

        due.setCompensatedAmount(dueCompensated.add(value));
        due.setOpenAmount(newDueOpen);
        due.setStatus(
                newDueOpen.signum() == 0
                        ? "PAID"
                        : "PARTIALLY_PAID");

        SupplierCreditNoteCompensation compensation =
                new SupplierCreditNoteCompensation();
        compensation.setTenantId(tenantId);
        compensation.setCompanyId(companyId);
        compensation.setCreditNoteFinancialPositionId(positionId);
        compensation.setPaymentDueId(paymentDueId);
        compensation.setOperationKey(key);
        compensation.setCompensationDate(compensationDate);
        compensation.setAmount(value);
        compensation.setNotes(notes);

        positions.save(position);
        dues.save(due);

        SupplierCreditNoteCompensation saved =
                compensations.saveAndFlush(compensation);

        if (saved == null || saved.getId() == null) {
            throw new BusinessException(
                    "Compensazione non salvata correttamente.");
        }

        return saved.getId();
    }

    private static BigDecimal requireMoney(BigDecimal amount) {
        if (amount == null || amount.scale() > 2
                || amount.signum() <= 0) {
            throw new BusinessException(
                    "Importo compensazione non valido.");
        }
        return amount.setScale(2);
    }

    private static BigDecimal requireStoredMoney(BigDecimal amount) {
        if (amount == null || amount.scale() > 2) {
            throw new BusinessException(
                    "Importo persistito non valido.");
        }
        return amount.setScale(2);
    }
}
