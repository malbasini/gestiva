package com.gestiva.accounting.payment.web;

import com.gestiva.accounting.due.entity.PaymentDue;
import com.gestiva.accounting.due.repository.PaymentDueRepository;
import com.gestiva.accounting.payment.entity.PaymentTransaction;
import com.gestiva.accounting.payment.repository.PaymentTransactionRepository;
import com.gestiva.crm.contact.entity.Customer;
import com.gestiva.crm.contact.repository.CustomerRepository;
import com.gestiva.documents.pdf.PdfFormatUtils;
import com.gestiva.purchasing.supplier.entity.Supplier;
import com.gestiva.purchasing.supplier.repository.SupplierRepository;
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
    private final CustomerRepository customerRepository;
    private final SupplierRepository supplierRepository;

    public PaymentTransactionListWebService(PaymentTransactionRepository paymentTransactionRepository,
                                            PaymentDueRepository paymentDueRepository,
                                            CustomerRepository customerRepository,
                                            SupplierRepository supplierRepository) {

        this.paymentTransactionRepository = paymentTransactionRepository;
        this.paymentDueRepository = paymentDueRepository;
        this.customerRepository = customerRepository;
        this.supplierRepository = supplierRepository;
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

        Set<Long> customerIds = new HashSet<>();
        Set<Long> supplierIds = new HashSet<>();

        for (PaymentTransaction tx : txPage.getContent()) {
            PaymentDue due = duesById.get(tx.getPaymentDueId());
            if (due == null || due.getPartyId() == null || due.getPartyType() == null) {
                continue;
            }

            if ("CUSTOMER".equalsIgnoreCase(due.getPartyType())) {
                customerIds.add(due.getPartyId());
            } else if ("SUPPLIER".equalsIgnoreCase(due.getPartyType())) {
                supplierIds.add(due.getPartyId());
            }
        }

        Map<Long, String> customerNamesById = customerRepository.findAllById(customerIds).stream()
                .collect(Collectors.toMap(
                        Customer::getId,
                        c -> buildCustomerLabel(c)
                ));

        Map<Long, String> supplierNamesById = supplierRepository.findAllById(supplierIds).stream()
                .collect(Collectors.toMap(
                        Supplier::getId,
                        s -> buildSupplierLabel(s)
                ));

        return txPage.map(tx -> toView(
                tx,
                duesById.get(tx.getPaymentDueId()),
                customerNamesById,
                supplierNamesById
        ));
    }
    private PaymentTransactionListItemView toView(PaymentTransaction tx,
                                                  PaymentDue due,
                                                  Map<Long, String> customerNamesById,
                                                  Map<Long, String> supplierNamesById) {
        PaymentTransactionListItemView v = new PaymentTransactionListItemView();
        v.setId(tx.getId());
        v.setPaymentDueId(tx.getPaymentDueId());
        v.setJournalEntryId(tx.getJournalEntryId());
        v.setFormattedPaymentDate(PdfFormatUtils.formatDate(tx.getPaymentDate()));
        v.setDirectionLabel(resolveDirectionLabel(tx.getDirection()));
        v.setCounterpartyType(tx.getCounterpartyType());
        v.setPartyLabel(buildPartyLabel(tx, due, customerNamesById, supplierNamesById));
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

    private String buildDocumentLabel(PaymentDue due) {
        if (due == null) {
            return "-";
        }

        String refType = due.getReferenceType() != null ? due.getReferenceType() : "";
        String docNo = due.getDocumentNumber() != null ? due.getDocumentNumber() : "-";

        if (refType.isBlank()) {
            return docNo;
        }

        if (refType.equalsIgnoreCase("SUPPLIER_INVOICE")) {
            docNo = "Fattura fornitore " + docNo;
        }

        if (refType.equalsIgnoreCase("CUSTOMER_INVOICE")){
           docNo = "Fattura cliente "+ docNo;
        }
        return docNo;
    }

    private String buildPartyLabel(PaymentTransaction tx,
                                   PaymentDue due,
                                   Map<Long, String> customerNamesById,
                                   Map<Long, String> supplierNamesById) {
        if (due != null && due.getPartyType() != null && due.getPartyId() != null) {
            if ("CUSTOMER".equalsIgnoreCase(due.getPartyType())) {
                return customerNamesById.getOrDefault(due.getPartyId(), "Cliente #" + due.getPartyId());
            }

            if ("SUPPLIER".equalsIgnoreCase(due.getPartyType())) {
                return supplierNamesById.getOrDefault(due.getPartyId(), "Fornitore #" + due.getPartyId());
            }
        }

        if (tx.getCounterpartyType() != null && tx.getCounterpartyId() != null) {
            if ("CUSTOMER".equalsIgnoreCase(tx.getCounterpartyType())) {
                return customerNamesById.getOrDefault(tx.getCounterpartyId(), "Cliente #" + tx.getCounterpartyId());
            }

            if ("SUPPLIER".equalsIgnoreCase(tx.getCounterpartyType())) {
                return supplierNamesById.getOrDefault(tx.getCounterpartyId(), "Fornitore #" + tx.getCounterpartyId());
            }
        }

        return "-";
    }
    private String buildCustomerLabel(Customer customer) {
        return customer.getName();
    }

    private String buildSupplierLabel(Supplier supplier) {
        return supplier.getName();
    }
}