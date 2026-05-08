package com.gestiva.accounting.due.service;

import com.gestiva.accounting.due.entity.PaymentDueTransaction;
import com.gestiva.accounting.due.repository.PaymentDueRepository;
import com.gestiva.accounting.due.repository.PaymentDueTransactionRepository;
import com.gestiva.accounting.due.web.PaymentDueRegistrationForm;
import com.gestiva.common.exception.BusinessException;
import com.gestiva.common.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Transactional
public class PaymentDueRegistrationService {

    private final PaymentDueRepository paymentDueRepository;
    private final PaymentDueTransactionRepository paymentDueTransactionRepository;

    public PaymentDueRegistrationService(PaymentDueRepository paymentDueRepository,
                                         PaymentDueTransactionRepository paymentDueTransactionRepository) {
        this.paymentDueRepository = paymentDueRepository;
        this.paymentDueTransactionRepository = paymentDueTransactionRepository;
    }

    public void registerMovement(Long tenantId, Long paymentDueId, PaymentDueRegistrationForm form) {
        var due = paymentDueRepository.findByTenantIdAndId(tenantId, paymentDueId)
                .orElseThrow(() -> new NotFoundException("Scadenza non trovata"));

        if ("PAID".equalsIgnoreCase(due.getStatus()) || "CANCELLED".equalsIgnoreCase(due.getStatus())) {
            throw new BusinessException("Su questa scadenza non è possibile registrare movimenti.");
        }

        BigDecimal amount = scale(form.getAmount());

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("L'importo deve essere maggiore di zero.");
        }

        if (amount.compareTo(due.getOpenAmount()) > 0) {
            throw new BusinessException("L'importo non può superare il residuo aperto.");
        }

        PaymentDueTransaction tx = new PaymentDueTransaction();
        tx.setTenantId(tenantId);
        tx.setPaymentDueId(due.getId());
        tx.setTransactionDate(form.getTransactionDate());
        tx.setAmount(amount);
        tx.setDirection("RECEIVABLE".equalsIgnoreCase(due.getDirection()) ? "RECEIPT" : "PAYMENT");
        tx.setNotes(form.getNotes());

        paymentDueTransactionRepository.save(tx);

        BigDecimal newPaid = scale(due.getPaidAmount().add(amount));
        BigDecimal newOpen = scale(due.getGrossAmount().subtract(newPaid));

        due.setPaidAmount(newPaid);
        due.setOpenAmount(newOpen);

        if (newOpen.compareTo(BigDecimal.ZERO) == 0) {
            due.setStatus("PAID");
        } else if (newPaid.compareTo(BigDecimal.ZERO) > 0) {
            due.setStatus("PARTIALLY_PAID");
        } else {
            due.setStatus("OPEN");
        }

        paymentDueRepository.save(due);
    }

    private BigDecimal scale(BigDecimal value) {
        return value == null
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : value.setScale(2, RoundingMode.HALF_UP);
    }
}