package com.gestiva.billing.invoice.web;

import com.gestiva.billing.invoice.dto.InvoiceSearchRequest;
import com.gestiva.billing.invoice.entity.Invoice;
import com.gestiva.billing.invoice.repository.InvoiceRepository;
import com.gestiva.billing.invoice.repository.InvoiceSpecifications;
import com.gestiva.common.dto.PageResponse;
import com.gestiva.crm.contact.entity.Customer;
import com.gestiva.crm.contact.repository.CustomerRepository;
import com.gestiva.documents.pdf.PdfFormatUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class InvoiceWebService {

    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;

    public InvoiceWebService(InvoiceRepository invoiceRepository,
                             CustomerRepository customerRepository) {
        this.invoiceRepository = invoiceRepository;
        this.customerRepository = customerRepository;
    }

    public PageResponse<InvoiceListItemView> search(Long tenantId,
                                                    InvoiceSearchRequest request,
                                                    Pageable pageable) {

        var specification = InvoiceSpecifications.hasTenantId(tenantId)
                .and(InvoiceSpecifications.matchesSearch(request.getSearch()))
                .and(InvoiceSpecifications.hasStatus(request.getStatus()))
                .and(InvoiceSpecifications.hasCustomerId(request.getCustomerId()))
                .and(InvoiceSpecifications.hasDeliveryNoteId(request.getDeliveryNoteId()));

        var page = invoiceRepository.findAll(specification, pageable);

        var customerIds = page.getContent().stream()
                .map(i -> i.getCustomerId())
                .collect(Collectors.toSet());

        Map<Long, String> customerNames = customerRepository.findAllById(customerIds).stream()
                .collect(Collectors.toMap(c -> c.getId(), c -> c.getName()));

        var content = page.getContent().stream().map(invoice -> {
            InvoiceListItemView item = new InvoiceListItemView();
            item.setId(invoice.getId());
            item.setInvoiceNumber(invoice.getInvoiceNumber());
            item.setFormattedInvoiceDate(PdfFormatUtils.formatDate(invoice.getInvoiceDate()));
            item.setCustomerName(customerNames.getOrDefault(invoice.getCustomerId(), "Cliente"));
            item.setStatus(invoice.getStatus());
            item.setDeliveryNoteId(invoice.getDeliveryNoteId());
            item.setFormattedTotalAmount(PdfFormatUtils.formatMoney(invoice.getTotalAmount()));
            return item;
        }).toList();

        PageResponse<InvoiceListItemView> response = new PageResponse<>();
        response.setContent(content);
        response.setPage(page.getNumber());
        response.setSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setFirst(page.isFirst());
        response.setLast(page.isLast());
        return response;
    }

    public Page<InvoiceListItemView> findPage(Long tenantId,
                                              int page,
                                              int size,
                                              String q,
                                              String status,
                                              LocalDate dateFrom,
                                              LocalDate dateTo) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Order.desc("invoiceDate"), Sort.Order.desc("id"))
        );
        Specification<Invoice> spec = Specification.where(byTenant(tenantId))
                .and(bySearch(q))
                .and(byStatus(status))
                .and(byDateFrom(dateFrom))
                .and(byDateTo(dateTo));
        return invoiceRepository.findAll(spec, pageable).map(this::toListItemView);

    }
    private Specification<Invoice> byTenant(Long tenantId) {
        return (root, query, cb) -> cb.equal(root.get("tenantId"), tenantId);
    }
    private Specification<Invoice> bySearch(String q) {
        if (q == null || q.trim().isEmpty()) {
            return null;
        }
        String like = "%" + q.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("invoiceNumber")), like);
    }
    private Specification<Invoice> byStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }
    private Specification<Invoice> byDateFrom(LocalDate dateFrom) {
        if (dateFrom == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("invoiceDate"), dateFrom);
    }
    private Specification<Invoice> byDateTo(LocalDate dateTo) {
        if (dateTo == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("invoiceDate"), dateTo);
    }
    private InvoiceListItemView toListItemView(Invoice invoice) {
        InvoiceListItemView v = new InvoiceListItemView();
        Customer customer = customerRepository.findById(invoice.getCustomerId()).orElse(null);
        if (customer != null) {
            v.setCustomerName(customer.getName());
        }
        v.setId(invoice.getId());
        v.setInvoiceNumber(invoice.getInvoiceNumber());
        v.setFormattedInvoiceDate(PdfFormatUtils.formatDate(invoice.getInvoiceDate()));
        v.setStatus(invoice.getStatus());
        v.setFormattedTotalAmount(PdfFormatUtils.formatMoney(invoice.getTotalAmount()));
        v.setCurrencyCode(invoice.getCurrencyCode());
        return v;

    }



}