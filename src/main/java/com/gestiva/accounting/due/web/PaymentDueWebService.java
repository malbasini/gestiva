package com.gestiva.accounting.due.web;

import com.gestiva.accounting.due.entity.PaymentDueTransaction;
import com.gestiva.accounting.due.repository.PaymentDueRepository;
import com.gestiva.accounting.due.repository.PaymentDueTransactionRepository;
import com.gestiva.documents.pdf.PdfFormatUtils;
import com.gestiva.purchasing.supplier.repository.SupplierRepository;
import com.gestiva.crm.contact.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class PaymentDueWebService {

    private final PaymentDueRepository paymentDueRepository;
    private final CustomerRepository customerRepository;
    private final SupplierRepository supplierRepository;
    private final PaymentDueTransactionRepository paymentDueTransactionRepository;

    public PaymentDueWebService(PaymentDueRepository paymentDueRepository,
                                CustomerRepository customerRepository,
                                SupplierRepository supplierRepository,
                                PaymentDueTransactionRepository paymentDueTransaction) {
        this.paymentDueRepository = paymentDueRepository;
        this.customerRepository = customerRepository;
        this.supplierRepository = supplierRepository;
        this.paymentDueTransactionRepository = paymentDueTransaction;
    }

    public List<PaymentDueListItemView> findAll(Long tenantId, String direction, boolean openOnly) {
        List<String> openStatuses = List.of("OPEN", "PARTIALLY_PAID");

        var dues = (direction != null && !direction.isBlank())
                ? (openOnly
                   ? paymentDueRepository.findByTenantIdAndDirectionAndStatusInOrderByDueDateAscIdAsc(tenantId, direction, openStatuses)
                   : paymentDueRepository.findByTenantIdAndDirectionOrderByDueDateAscIdAsc(tenantId, direction))
                : (openOnly
                   ? paymentDueRepository.findByTenantIdAndStatusInOrderByDueDateAscIdAsc(tenantId, openStatuses)
                   : paymentDueRepository.findByTenantIdOrderByDueDateAscIdAsc(tenantId));

        LocalDate today = LocalDate.now();

        return dues.stream().map(due -> {
            PaymentDueListItemView v = new PaymentDueListItemView();
            v.setId(due.getId());
            v.setDirection(due.getDirection());
            v.setPartyName(resolvePartyName(tenantId, due.getPartyType(), due.getPartyId()));
            v.setDocumentNumber(due.getDocumentNumber());
            v.setFormattedDocumentDate(PdfFormatUtils.formatDate(due.getDocumentDate()));
            v.setFormattedDueDate(PdfFormatUtils.formatDate(due.getDueDate()));
            v.setFormattedGrossAmount(PdfFormatUtils.formatMoney(due.getGrossAmount()));
            v.setFormattedPaidAmount(PdfFormatUtils.formatMoney(due.getPaidAmount()));
            v.setFormattedOpenAmount(PdfFormatUtils.formatMoney(due.getOpenAmount()));
            v.setStatus(due.getStatus());
            v.setOverdue(
                    ("OPEN".equalsIgnoreCase(due.getStatus()) || "PARTIALLY_PAID".equalsIgnoreCase(due.getStatus()))
                            && due.getDueDate() != null
                            && due.getDueDate().isBefore(today)
            );
            v.setReferenceType(due.getReferenceType());
            v.setReferenceId(due.getReferenceId());
            return v;
        }).toList();
    }

    private String resolvePartyName(Long tenantId, String partyType, Long partyId) {
        if ("CUSTOMER".equalsIgnoreCase(partyType)) {
            return customerRepository.findByTenantIdAndId(tenantId, partyId)
                    .map(c -> c.getName())
                    .orElse("-");
        }

        if ("SUPPLIER".equalsIgnoreCase(partyType)) {
            return supplierRepository.findByTenantIdAndId(tenantId, partyId)
                    .map(s -> s.getName())
                    .orElse("-");
        }

        return "-";
    }

    public PaymentDueDetailView getDetail(Long tenantId, Long id) {
        var due = paymentDueRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new com.gestiva.common.exception.NotFoundException("Scadenza non trovata"));

        var transactions = paymentDueTransactionRepository.findByTenantIdAndPaymentDueIdOrderByTransactionDateAscIdAsc(tenantId, id);

        java.time.LocalDate today = java.time.LocalDate.now();

        PaymentDueDetailView v = new PaymentDueDetailView();
        v.setId(due.getId());
        v.setDirection(due.getDirection());
        v.setPartyName(resolvePartyName(tenantId, due.getPartyType(), due.getPartyId()));
        v.setDocumentNumber(due.getDocumentNumber());
        v.setFormattedDocumentDate(PdfFormatUtils.formatDate(due.getDocumentDate()));
        v.setFormattedDueDate(PdfFormatUtils.formatDate(due.getDueDate()));
        v.setFormattedGrossAmount(PdfFormatUtils.formatMoney(due.getGrossAmount()));
        v.setFormattedPaidAmount(PdfFormatUtils.formatMoney(due.getPaidAmount()));
        v.setFormattedOpenAmount(PdfFormatUtils.formatMoney(due.getOpenAmount()));
        v.setStatus(due.getStatus());
        v.setOverdue(
                ("OPEN".equalsIgnoreCase(due.getStatus()) || "PARTIALLY_PAID".equalsIgnoreCase(due.getStatus()))
                        && due.getDueDate() != null
                        && due.getDueDate().isBefore(today)
        );
        v.setReferenceType(due.getReferenceType());
        v.setReferenceId(due.getReferenceId());
        v.setCanRegisterMovement(!"PAID".equalsIgnoreCase(due.getStatus()) && !"CANCELLED".equalsIgnoreCase(due.getStatus()));
        v.setRegisterButtonLabel("RECEIVABLE".equalsIgnoreCase(due.getDirection()) ? "Registra incasso" : "Registra pagamento");

        for (var tx : transactions) {
            PaymentDueTransactionView tv = new PaymentDueTransactionView();
            tv.setFormattedTransactionDate(PdfFormatUtils.formatDate(tx.getTransactionDate()));
            tv.setDirection(tx.getDirection());
            tv.setFormattedAmount(PdfFormatUtils.formatMoney(tx.getAmount()));
            tv.setNotes(tx.getNotes());
            v.getTransactions().add(tv);
        }

        return v;
    }
}
