package com.gestiva.accounting.payment.web;

import com.gestiva.accounting.due.entity.PaymentDue;
import com.gestiva.accounting.due.repository.PaymentDueRepository;
import com.gestiva.accounting.payment.entity.PaymentTransaction;
import com.gestiva.accounting.payment.repository.PaymentTransactionRepository;
import com.gestiva.documents.pdf.PdfFormatUtils;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PaymentTransactionListWebService {

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final PaymentDueRepository paymentDueRepository;

    public PaymentTransactionListWebService(PaymentTransactionRepository paymentTransactionRepository,
                                            PaymentDueRepository paymentDueRepository) {
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.paymentDueRepository = paymentDueRepository;
    }

    public Page<PaymentTransactionListItemView> findPage(Long tenantId,
                                                         int page,
                                                         int size,
                                                         String direction) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Order.desc("paymentDate"), Sort.Order.desc("id"))
        );

        Page<PaymentTransaction> txPage;

        if (direction != null && !direction.isBlank()) {
            txPage = paymentTransactionRepository.findByTenantIdAndDirection(
                    tenantId,
                    direction.trim().toUpperCase(),
                    pageable
            );
        } else {
            txPage = paymentTransactionRepository.findByTenantId(tenantId, pageable);
        }

        Set<Long> dueIds = txPage.getContent().stream()
                .map(PaymentTransaction::getPaymentDueId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, PaymentDue> duesById = paymentDueRepository.findAllById(dueIds).stream()
                .collect(Collectors.toMap(PaymentDue::getId, Function.identity()));

        return txPage.map(tx -> toView(tx, duesById.get(tx.getPaymentDueId())));
    }

    private PaymentTransactionListItemView toView(PaymentTransaction tx, PaymentDue due) {
        PaymentTransactionListItemView v = new PaymentTransactionListItemView();
        v.setId(tx.getId());
        v.setPaymentDueId(tx.getPaymentDueId());
        v.setJournalEntryId(tx.getJournalEntryId());
        v.setFormattedPaymentDate(PdfFormatUtils.formatDate(tx.getPaymentDate()));
        v.setDirectionLabel(resolveDirectionLabel(tx.getDirection()));
        v.setCounterpartyType(tx.getCounterpartyType());
        v.setPartyLabel(buildPartyLabel(tx, due));
        v.setDocumentLabel(buildDocumentLabel(due));
        v.setFormattedAmount(PdfFormatUtils.formatMoney(tx.getAmount()));
        v.setPaymentMethod(tx.getPaymentMethod());
        v.setReference(tx.getReference());
        v.setDueStatus(due != null ? due.getStatus() : "-");
        return v;
    }

    private String resolveDirectionLabel(String direction) {
        if ("IN".equalsIgnoreCase(direction)) {
            return "Incasso";
        }
        if ("OUT".equalsIgnoreCase(direction)) {
            return "Pagamento";
        }
        return direction != null ? direction : "-";
    }

    private String buildPartyLabel(PaymentTransaction tx, PaymentDue due) {
        if (due != null) {
            return due.getPartyType() + " #" + due.getPartyId();
        }
        if (tx.getCounterpartyType() != null && tx.getCounterpartyId() != null) {
            return tx.getCounterpartyType() + " #" + tx.getCounterpartyId();
        }
        return "-";
    }

    private String buildDocumentLabel(PaymentDue due) {
        if (due == null) {
            return "-";
        }
        return due.getDocumentNumber() != null ? due.getDocumentNumber() : "-";
    }
}