package com.gestiva.accounting.due.web;

import com.gestiva.accounting.due.repository.PaymentDueRepository;
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

    public PaymentDueWebService(PaymentDueRepository paymentDueRepository,
                                CustomerRepository customerRepository,
                                SupplierRepository supplierRepository) {
        this.paymentDueRepository = paymentDueRepository;
        this.customerRepository = customerRepository;
        this.supplierRepository = supplierRepository;
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
}
