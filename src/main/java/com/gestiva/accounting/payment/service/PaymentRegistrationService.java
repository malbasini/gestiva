package com.gestiva.accounting.payment.service;

import com.gestiva.accounting.due.entity.PaymentDue;
import com.gestiva.accounting.due.repository.PaymentDueRepository;
import com.gestiva.accounting.payment.entity.PaymentTransaction;
import com.gestiva.accounting.payment.repository.PaymentTransactionRepository;
import com.gestiva.accounting.payment.web.PaymentRegistrationForm;
import com.gestiva.common.exception.BusinessException;
import com.gestiva.common.util.NumberInputUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Transactional
public class PaymentRegistrationService {

    private final PaymentDueRepository paymentDueRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final PaymentAccountingService paymentAccountingService;

    public PaymentRegistrationService(PaymentDueRepository paymentDueRepository,
                                      PaymentTransactionRepository paymentTransactionRepository,
                                      PaymentAccountingService paymentAccountingService) {
        this.paymentDueRepository = paymentDueRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.paymentAccountingService = paymentAccountingService;
    }

    public Long register(Long tenantId, PaymentRegistrationForm form) {
        PaymentDue due = paymentDueRepository.findByTenantIdAndId(tenantId, form.getPaymentDueId())
                .orElseThrow(() -> new BusinessException("Scadenza non trovata."));

        if ("PAID".equalsIgnoreCase(due.getStatus())) {
            throw new BusinessException("La scadenza risulta già saldata.");
        }

        if ("CANCELLED".equalsIgnoreCase(due.getStatus())) {
            throw new BusinessException("La scadenza è annullata e non può essere movimentata.");
        }

        BigDecimal amount = NumberInputUtils.parseDecimal(form.getAmount(), "l'importo")
                .setScale(2, RoundingMode.HALF_UP);

        if (amount.signum() <= 0) {
            throw new BusinessException("L'importo deve essere maggiore di zero.");
        }

        BigDecimal grossAmount = nvlMoney(due.getGrossAmount());
        BigDecimal paidAmount = nvlMoney(due.getPaidAmount());
        BigDecimal openAmount = nvlMoney(due.getOpenAmount());

        if (openAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("La scadenza non ha importo residuo aperto.");
        }

        if (amount.compareTo(openAmount) > 0) {
            throw new BusinessException("L'importo supera il residuo della scadenza.");
        }

        PaymentTransaction tx = new PaymentTransaction();
        tx.setTenantId(tenantId);
        tx.setDirection(resolveTransactionDirection(due));
        tx.setCounterpartyType(due.getPartyType());
        tx.setCounterpartyId(due.getPartyId());
        tx.setPaymentDueId(due.getId());
        tx.setDocumentType(due.getReferenceType());
        tx.setDocumentId(due.getReferenceId());
        tx.setPaymentDate(form.getPaymentDate());
        tx.setAmount(amount);
        tx.setPaymentMethod(form.getPaymentMethod());
        tx.setReference(form.getReference());
        tx.setNotes(form.getNotes());

        PaymentTransaction saved = paymentTransactionRepository.save(tx);

        Long journalEntryId = paymentAccountingService.createAccountingEntryForPayment(
                tenantId,
                due,
                form.getPaymentDate(),
                amount,
                form.getFinancialAccountId(),
                form.getReference(),
                form.getNotes()
        );

        saved.setJournalEntryId(journalEntryId);
        paymentTransactionRepository.save(saved);

        BigDecimal newPaidAmount = paidAmount.add(amount).setScale(2, RoundingMode.HALF_UP);
        BigDecimal newOpenAmount = grossAmount.subtract(newPaidAmount).setScale(2, RoundingMode.HALF_UP);

        if (newOpenAmount.compareTo(BigDecimal.ZERO) < 0) {
            newOpenAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        due.setPaidAmount(newPaidAmount);
        due.setOpenAmount(newOpenAmount);
        due.setStatus(resolveDueStatus(newOpenAmount, grossAmount));

        paymentDueRepository.save(due);

        return saved.getId();
    }

    private String resolveTransactionDirection(PaymentDue due) {
        if ("RECEIVABLE".equalsIgnoreCase(due.getDirection())) {
            return "IN";
        }
        if ("PAYABLE".equalsIgnoreCase(due.getDirection())) {
            return "OUT";
        }
        throw new BusinessException("Direzione scadenza non valida: " + due.getDirection());
    }

    private String resolveDueStatus(BigDecimal openAmount, BigDecimal grossAmount) {
        if (openAmount.compareTo(BigDecimal.ZERO) == 0) {
            return "PAID";
        }

        if (openAmount.compareTo(grossAmount) < 0) {
            return "PARTIALLY_PAID";
        }

        return "OPEN";
    }

    private BigDecimal nvlMoney(BigDecimal value) {
        return value == null
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : value.setScale(2, RoundingMode.HALF_UP);
    }
}