package com.gestiva.accounting.due.service;

import com.gestiva.accounting.due.entity.PaymentDue;
import com.gestiva.accounting.due.repository.PaymentDueRepository;
import com.gestiva.billing.invoice.entity.Invoice;
import com.gestiva.billing.invoice.repository.InvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
@Transactional
public class PaymentDueService {

    private final PaymentDueRepository paymentDueRepository;
    private final InvoiceRepository invoiceRepository;

    public PaymentDueService(PaymentDueRepository paymentDueRepository,
                             InvoiceRepository invoiceRepository) {
        this.paymentDueRepository = paymentDueRepository;
        this.invoiceRepository = invoiceRepository;
    }

    public Long createReceivableFromCustomerInvoice(Long tenantId,
                                                    Long customerId,
                                                    String invoiceNumber,
                                                    LocalDate invoiceDate,
                                                    String currencyCode,
                                                    BigDecimal totalAmount,
                                                    Long invoiceId) {

        return createDue(
                tenantId,
                "RECEIVABLE",
                "CUSTOMER",
                customerId,
                invoiceNumber,
                invoiceDate,
                invoiceDate.plusDays(30),
                "CUSTOMER_INVOICE",
                invoiceId,
                currencyCode,
                totalAmount,
                "Scadenza generata da fattura cliente"
        );
    }

    public Long createPayableFromSupplierInvoice(Long tenantId,
                                                 Long supplierId,
                                                 String invoiceNumber,
                                                 LocalDate invoiceDate,
                                                 String currencyCode,
                                                 BigDecimal totalAmount,
                                                 Long invoiceId) {

        return createDue(
                tenantId,
                "PAYABLE",
                "SUPPLIER",
                supplierId,
                invoiceNumber,
                invoiceDate,
                invoiceDate.plusDays(30),
                "SUPPLIER_INVOICE",
                invoiceId,
                currencyCode,
                totalAmount,
                "Scadenza generata da fattura fornitore"
        );
    }

    private Long createDue(Long tenantId,
                           String direction,
                           String partyType,
                           Long partyId,
                           String documentNumber,
                           LocalDate documentDate,
                           LocalDate dueDate,
                           String referenceType,
                           Long referenceId,
                           String currencyCode,
                           BigDecimal grossAmount,
                           String notes) {

        PaymentDue due = new PaymentDue();
        due.setTenantId(tenantId);
        due.setDirection(direction);
        due.setPartyType(partyType);
        due.setPartyId(partyId);
        due.setDocumentNumber(documentNumber);
        due.setDocumentDate(documentDate);
        due.setDueDate(dueDate);
        due.setReferenceType(referenceType);
        due.setReferenceId(referenceId);
        due.setCurrencyCode(currencyCode);

        BigDecimal amount = scale(grossAmount);
        due.setGrossAmount(amount);
        due.setPaidAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        due.setOpenAmount(amount);
        due.setStatus("OPEN");
        due.setNotes(notes);
        var existing = paymentDueRepository.findByTenantIdAndReferenceTypeAndReferenceId(
                tenantId, referenceType, referenceId
        );
        if (existing.isPresent()) {
            return existing.get().getId();
        }
        Invoice invoice = invoiceRepository.findByTenantIdAndId(tenantId, referenceId).orElse(null);
        if (invoice != null) {
            invoice.setStatus("ISSUED");
            invoiceRepository.save(invoice);
        }
        return paymentDueRepository.save(due).getId();
    }

    private BigDecimal scale(BigDecimal value) {
        return value == null
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : value.setScale(2, RoundingMode.HALF_UP);
    }
}