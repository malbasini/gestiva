package com.gestiva.billing.invoice.web;

import com.gestiva.billing.invoice.dto.InvoiceSearchRequest;
import com.gestiva.billing.invoice.repository.InvoiceRepository;
import com.gestiva.billing.invoice.repository.InvoiceSpecifications;
import com.gestiva.common.dto.PageResponse;
import com.gestiva.crm.contact.repository.CustomerRepository;
import com.gestiva.documents.pdf.PdfFormatUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
}