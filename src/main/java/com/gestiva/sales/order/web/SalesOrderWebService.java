package com.gestiva.sales.order.web;

import com.gestiva.common.dto.PageResponse;
import com.gestiva.crm.contact.entity.Customer;
import com.gestiva.crm.contact.repository.CustomerRepository;
import com.gestiva.documents.pdf.PdfFormatUtils;
import com.gestiva.sales.order.dto.SalesOrderSearchRequest;
import com.gestiva.sales.order.entity.SalesOrder;
import com.gestiva.sales.order.repository.SalesOrderRepository;
import com.gestiva.sales.order.repository.SalesOrderSpecifications;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional(readOnly = true)
public class SalesOrderWebService {

    private final SalesOrderRepository salesOrderRepository;
    private final CustomerRepository customerRepository;

    public SalesOrderWebService(SalesOrderRepository salesOrderRepository,
                                CustomerRepository customerRepository) {

        this.salesOrderRepository = salesOrderRepository;
        this.customerRepository = customerRepository;
    }

    public PageResponse<SalesOrderListItemView> search(Long tenantId,
                                                       SalesOrderSearchRequest request,
                                                       Pageable pageable) {

        var specification = SalesOrderSpecifications.hasTenantId(tenantId)
                .and(SalesOrderSpecifications.hasStatus(request.getStatus()))
                .and(SalesOrderSpecifications.hasCustomerId(request.getCustomerId()))
                .and(SalesOrderSpecifications.hasQuoteId(request.getQuoteId()))
                .and(SalesOrderSpecifications.matchesSearch(request.getSearch()));

        var page = salesOrderRepository.findAll(specification, pageable);

        var content = page.getContent().stream().map(order -> {
            SalesOrderListItemView item = new SalesOrderListItemView();
            item.setId(order.getId());
            item.setCustomerId(order.getCustomerId());
            item.setQuoteId(order.getQuoteId());
            item.setOrderNumber(order.getOrderNumber());
            item.setOrderDate(order.getOrderDate());
            item.setStatus(order.getStatus());
            item.setCurrencyCode(order.getCurrencyCode());
            item.setTotalAmount(order.getTotalAmount());
            item.setFormattedOrderDate(PdfFormatUtils.formatDate(order.getOrderDate()));
            item.setFormattedTotalAmount(PdfFormatUtils.formatMoney(order.getTotalAmount()));
            return item;
        }).toList();

        PageResponse<SalesOrderListItemView> response = new PageResponse<>();
        response.setContent(content);
        response.setPage(page.getNumber());
        response.setSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setFirst(page.isFirst());
        response.setLast(page.isLast());
        return response;
    }
    public Page<SalesOrderListItemView> findPage(Long tenantId,
                                                 int page,
                                                 int size,
                                                 String q,
                                                 String status,
                                                 LocalDate dateFrom,
                                                 LocalDate dateTo) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Order.desc("orderDate"), Sort.Order.desc("id"))
        );
        Specification<SalesOrder> spec = Specification.where(byTenant(tenantId))
                .and(bySearch(q))
                .and(byStatus(status))
                .and(byDateFrom(dateFrom))
                .and(byDateTo(dateTo));
        return salesOrderRepository.findAll(spec, pageable).map(this::toListItemView);
    }
    private Specification<SalesOrder> byTenant(Long tenantId) {
        return (root, query, cb) -> cb.equal(root.get("tenantId"), tenantId);
    }
    private Specification<SalesOrder> bySearch(String q) {
        if (q == null || q.trim().isEmpty()) {
            return null;
        }
        String like = "%" + q.trim().toLowerCase() + "%";
        return (root, query, cb) -> {
            Join<Object, Object> customer = root.join("customer", JoinType.LEFT);
            return cb.or(
                    cb.like(cb.lower(root.get("orderNumber")), like),
                    cb.like(cb.lower(customer.get("name")), like)
            );
        };
    }
    private Specification<SalesOrder> byStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }
    private Specification<SalesOrder> byDateFrom(LocalDate dateFrom) {
        if (dateFrom == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("orderDate"), dateFrom);
    }
    private Specification<SalesOrder> byDateTo(LocalDate dateTo) {
        if (dateTo == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("orderDate"), dateTo);
    }
    private SalesOrderListItemView toListItemView(SalesOrder order) {
        SalesOrderListItemView v = new SalesOrderListItemView();
        Customer customer = customerRepository.findById(order.getCustomerId()).orElse(null);
        if (customer != null) {
            v.setCustomerName(customer.getName());
        }
        v.setId(order.getId());
        v.setOrderNumber(order.getOrderNumber());
        v.setFormattedOrderDate(PdfFormatUtils.formatDate(order.getOrderDate()));
        v.setStatus(order.getStatus());
        v.setFormattedTotalAmount(PdfFormatUtils.formatMoney(order.getTotalAmount()));
        v.setCurrencyCode(order.getCurrencyCode());
        return v;
    }
}