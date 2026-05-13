package com.gestiva.logistics.ddt.web;

import com.gestiva.crm.contact.entity.Customer;
import com.gestiva.crm.contact.repository.CustomerRepository;
import com.gestiva.documents.pdf.PdfFormatUtils;
import com.gestiva.logistics.ddt.entity.DeliveryNote;
import com.gestiva.logistics.ddt.repository.DeliveryNoteRepository;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

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

    public Page<DeliveryNoteListItemView> findPage(Long tenantId,
                                                   int page,
                                                   int size,
                                                   String q,
                                                   String status,
                                                   LocalDate dateFrom,
                                                   LocalDate dateTo) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Order.desc("ddtDate"), Sort.Order.desc("id"))
        );

        Specification<DeliveryNote> spec = Specification.where(byTenant(tenantId))
                .and(bySearch(q))
                .and(byStatus(status))
                .and(byDateFrom(dateFrom))
                .and(byDateTo(dateTo));

        return deliveryNoteRepository.findAll(spec, pageable).map(this::toListItemView);
    }

    private Specification<DeliveryNote> byTenant(Long tenantId) {
        return (root, query, cb) -> cb.equal(root.get("tenantId"), tenantId);
    }

    private Specification<DeliveryNote> bySearch(String q) {
        if (q == null || q.trim().isEmpty()) {
            return null;
        }

        String like = "%" + q.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("ddtNumber")), like);
    }

    private Specification<DeliveryNote> byStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    private Specification<DeliveryNote> byDateFrom(LocalDate dateFrom) {
        if (dateFrom == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("ddtDate"), dateFrom);
    }

    private Specification<DeliveryNote> byDateTo(LocalDate dateTo) {
        if (dateTo == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("ddtDate"), dateTo);
    }

    private DeliveryNoteListItemView toListItemView(DeliveryNote note) {
        DeliveryNoteListItemView v = new DeliveryNoteListItemView();
        Customer customer = customerRepository.findById(note.getCustomerId()).orElse(null);
        if (customer != null) {
            v.setCustomerName(customer.getName());
        }
        v.setId(note.getId());
        v.setDdtNumber(note.getDdtNumber());
        v.setFormattedDdtDate(PdfFormatUtils.formatDate(note.getDdtDate()));
        v.setStatus(note.getStatus());
        v.setFormattedTotalAmount(PdfFormatUtils.formatMoney(note.getTotalAmount()));
        v.setSalesOrderId(note.getSalesOrderId());
        return v;
    }
}