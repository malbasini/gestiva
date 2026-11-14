package com.gestiva.accounting.due.service;

import com.gestiva.accounting.due.entity.PaymentDueTransaction;
import com.gestiva.accounting.due.repository.PaymentDueRepository;
import com.gestiva.accounting.due.repository.PaymentDueTransactionRepository;
import com.gestiva.accounting.due.web.PaymentDueRegistrationForm;
import com.gestiva.accounting.v2.journal.service.JournalAutoPostingService;
import com.gestiva.common.exception.BusinessException;
import com.gestiva.common.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import com.gestiva.security.company.context.CompanyContext;
import java.math.RoundingMode;

@Service
@Transactional
public class PaymentDueRegistrationService {

    private final PaymentDueRepository paymentDueRepository;
    private final PaymentDueTransactionRepository paymentDueTransactionRepository;
    private final JournalAutoPostingService journalAutoPostingService;
    private final CompanyContext companyContext;


    public PaymentDueRegistrationService(
            PaymentDueRepository paymentDueRepository,
            PaymentDueTransactionRepository paymentDueTransactionRepository,
            JournalAutoPostingService journalAutoPostingService,
            CompanyContext companyContext) {

        this.paymentDueRepository = paymentDueRepository;
        this.paymentDueTransactionRepository = paymentDueTransactionRepository;
        this.journalAutoPostingService = journalAutoPostingService;
        this.companyContext = companyContext;
    }

    public void registerMovement(Long tenantId, Long paymentDueId, PaymentDueRegistrationForm form) {

        Long companyId = companyContext.resolveCurrentCompanyId();
        if (companyId == null) {
            throw new BusinessException("Nessuna società attiva selezionata.");
        }
        var due = paymentDueRepository
                .findByTenantIdAndCompanyIdAndIdForUpdate(
                        tenantId,
                        companyId,
                        paymentDueId
                )
                .orElseThrow(() -> new NotFoundException(
                        "Scadenza non trovata nella società attiva."
                ));

        if ("PAID".equalsIgnoreCase(due.getStatus()) || "CANCELLED".equalsIgnoreCase(due.getStatus())) {
            throw new BusinessException("Su questa scadenza non è possibile registrare movimenti.");
        }

        BigDecimal amount = scale(form.getAmount());

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("L'importo deve essere maggiore di zero.");
        }

        BigDecimal grossAmount = scale(due.getGrossAmount());
        BigDecimal paidAmount = scale(due.getPaidAmount());
        BigDecimal compensatedAmount = scale(due.getCompensatedAmount());
        BigDecimal openAmount = scale(due.getOpenAmount());

        if (grossAmount.compareTo(
                paidAmount.add(compensatedAmount).add(openAmount)) != 0) {
            throw new BusinessException(
                    "Scadenza non quadrata: importi pagati, compensati e residui incoerenti.");
        }

        if (openAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("La scadenza non ha importo residuo aperto.");
        }

        if (amount.compareTo(openAmount) > 0) {
            throw new BusinessException("L'importo non può superare il residuo aperto.");
        }

        PaymentDueTransaction tx = new PaymentDueTransaction();
        tx.setTenantId(tenantId);
        tx.setCompanyId(due.getCompanyId());
        tx.setPaymentDueId(due.getId());
        tx.setTransactionDate(form.getTransactionDate());
        tx.setAmount(amount);
        tx.setDirection("RECEIVABLE".equalsIgnoreCase(due.getDirection()) ? "RECEIPT" : "PAYMENT");
        tx.setNotes(form.getNotes());

        paymentDueTransactionRepository.save(tx);

        BigDecimal newPaid = scale(paidAmount.add(amount));
        BigDecimal newOpen = scale(
                grossAmount.subtract(newPaid).subtract(compensatedAmount));

        due.setPaidAmount(newPaid);
        due.setOpenAmount(newOpen);

        if (newOpen.compareTo(BigDecimal.ZERO) == 0) {
            due.setStatus("PAID");
        } else if (newOpen.compareTo(grossAmount) < 0) {
            due.setStatus("PARTIALLY_PAID");
        } else {
            due.setStatus("OPEN");
        }
        paymentDueRepository.save(due);
        if ("RECEIVABLE".equalsIgnoreCase(due.getDirection())) {
            journalAutoPostingService.postCustomerReceipt(
                    tenantId,
                    form.getTransactionDate(),
                    due.getDocumentNumber(),
                    amount,
                    due.getCurrencyCode(),
                    due.getId(),
                    form.getNotes()
            );
        } else {
            journalAutoPostingService.postSupplierPayment(
                    tenantId,
                    form.getTransactionDate(),
                    due.getDocumentNumber(),
                    amount,
                    due.getCurrencyCode(),
                    due.getId(),
                    form.getNotes()
            );
        }
    }

    private BigDecimal scale(BigDecimal value) {
        return value == null
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : value.setScale(2, RoundingMode.HALF_UP);
    }
}