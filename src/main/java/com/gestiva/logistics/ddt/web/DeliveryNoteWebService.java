package com.gestiva.logistics.ddt.web;

import com.gestiva.common.dto.PageResponse;
import com.gestiva.crm.contact.repository.CustomerRepository;
import com.gestiva.documents.pdf.PdfFormatUtils;
import com.gestiva.logistics.ddt.dto.DeliveryNoteSearchRequest;
import com.gestiva.logistics.ddt.repository.DeliveryNoteRepository;
import com.gestiva.logistics.ddt.repository.DeliveryNoteSpecifications;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DeliveryNoteWebService {

    private final DeliveryNoteRepository deliveryNoteRepository;
    private final CustomerRepository customerRepository;

    public DeliveryNoteWebService(DeliveryNoteRepository deliveryNoteRepository,
                                  CustomerRepository customerRepository) {
        this.deliveryNoteRepository = deliveryNoteRepository;
        this.customerRepository = customerRepository;
    }

    public PageResponse<DeliveryNoteListItemView> search(Long tenantId,
                                                         DeliveryNoteSearchRequest request,
                                                         Pageable pageable) {

        var specification = DeliveryNoteSpecifications.hasTenantId(tenantId)
                .and(DeliveryNoteSpecifications.matchesSearch(request.getSearch()))
                .and(DeliveryNoteSpecifications.hasStatus(request.getStatus()))
                .and(DeliveryNoteSpecifications.hasCustomerId(request.getCustomerId()))
                .and(DeliveryNoteSpecifications.hasSalesOrderId(request.getSalesOrderId()));

        var page = deliveryNoteRepository.findAll(specification, pageable);

        var customerIds = page.getContent().stream()
                .map(note -> note.getCustomerId())
                .collect(Collectors.toSet());

        Map<Long, String> customerNames = customerRepository.findAllById(customerIds).stream()
                .collect(Collectors.toMap(c -> c.getId(), c -> c.getName()));

        var content = page.getContent().stream().map(note -> {
            DeliveryNoteListItemView item = new DeliveryNoteListItemView();
            item.setId(note.getId());
            item.setDdtNumber(note.getDdtNumber());
            item.setFormattedDdtDate(PdfFormatUtils.formatDate(note.getDdtDate()));
            item.setCustomerName(customerNames.getOrDefault(note.getCustomerId(), "Cliente"));
            item.setStatus(note.getStatus());
            item.setSalesOrderId(note.getSalesOrderId());
            item.setFormattedTotalAmount(PdfFormatUtils.formatMoney(note.getTotalAmount()));
            return item;
        }).toList();

        PageResponse<DeliveryNoteListItemView> response = new PageResponse<>();
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